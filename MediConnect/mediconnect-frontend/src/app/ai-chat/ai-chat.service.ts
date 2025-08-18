import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { BehaviorSubject, Observable, Subject } from 'rxjs';
import { takeUntil } from 'rxjs/operators';

export interface AiChatMessage {
  id: string;
  role: 'user' | 'assistant';
  content: string;
  timestamp: Date;
  patientAdvice?: string;
  doctorSummary?: any;
  prescribedMedicines?: {
    name: string;
    dose: string;
    frequency: string;
    duration: string;
    otcOrPrescription: string;
  }[];
  riskLevel?: string;
  redFlags?: string[];
  homeRemedies?: string[];
  specializationHint?: string;
  liked?: boolean;
  disliked?: boolean;
  error?: boolean;
}

export interface ConversationContext {
  conversationId: string | null;
  sessionId: string | null;
  messageOrder: number;
  timestamp: number;
}

@Injectable({
  providedIn: 'root'
})
export class AiChatService {
  private readonly API_BASE = 'http://localhost:8080/api';
  private readonly MAX_MESSAGES = 100;
  private readonly MAX_HISTORY = 10;

  // State management
  private messages$ = new BehaviorSubject<AiChatMessage[]>([]);
  private loading$ = new BehaviorSubject<boolean>(false);
  private error$ = new BehaviorSubject<string | null>(null);
  private typing$ = new BehaviorSubject<boolean>(false);

  // Cache management
  private messagesCache = new Map<string, AiChatMessage[]>();
  private historyCache = new Map<number, string[]>();

  constructor(private http: HttpClient) {}

  // Public observables
  getMessages(): Observable<AiChatMessage[]> {
    return this.messages$.asObservable();
  }

  getLoadingState(): Observable<boolean> {
    return this.loading$.asObservable();
  }

  getErrorState(): Observable<string | null> {
    return this.error$.asObservable();
  }

  getTypingState(): Observable<boolean> {
    return this.typing$.asObservable();
  }

  // Conversation context management
  getConversationContext(patientId: number): ConversationContext | null {
    const stored = localStorage.getItem(`ai-chat-context-${patientId}`);
    if (stored) {
      try {
        const context = JSON.parse(stored);
        // Check if context is recent (within 1 hour)
        const timeDiff = Date.now() - (context.timestamp || 0);
        if (timeDiff < 60 * 60 * 1000) {
          return context;
        }
      } catch (e) {
        console.warn('Failed to load conversation context:', e);
      }
    }
    return null;
  }

  saveConversationContext(patientId: number, context: ConversationContext): void {
    localStorage.setItem(`ai-chat-context-${patientId}`, JSON.stringify({
      ...context,
      timestamp: Date.now()
    }));
  }

  clearConversationContext(patientId: number): void {
    localStorage.removeItem(`ai-chat-context-${patientId}`);
  }

  // History management
  getHistory(patientId: number): string[] {
    if (this.historyCache.has(patientId)) {
      return this.historyCache.get(patientId) || [];
    }

    const stored = localStorage.getItem(`ai-chat-history-${patientId}`);
    if (stored) {
      try {
        const history = JSON.parse(stored);
        this.historyCache.set(patientId, history);
        return history;
      } catch (e) {
        console.warn('Failed to load history:', e);
      }
    }
    return [];
  }

  addToHistory(patientId: number, question: string): void {
    const history = this.getHistory(patientId);
    history.unshift(question);
    const trimmedHistory = history.slice(0, this.MAX_HISTORY);
    
    this.historyCache.set(patientId, trimmedHistory);
    localStorage.setItem(`ai-chat-history-${patientId}`, JSON.stringify(trimmedHistory));
  }

  // Message management
  addMessage(message: AiChatMessage): void {
    const currentMessages = this.messages$.value;
    const updatedMessages = [...currentMessages, message];
    
    // Limit messages for performance
    const trimmedMessages = updatedMessages.length > this.MAX_MESSAGES 
      ? updatedMessages.slice(-this.MAX_MESSAGES)
      : updatedMessages;
    
    this.messages$.next(trimmedMessages);
    
    // Clear cache when new messages are added
    this.messagesCache.clear();
  }

  updateMessage(messageId: string, updates: Partial<AiChatMessage>): void {
    const currentMessages = this.messages$.value;
    const updatedMessages = currentMessages.map(msg => 
      msg.id === messageId ? { ...msg, ...updates } : msg
    );
    this.messages$.next(updatedMessages);
  }

  clearMessages(): void {
    this.messages$.next([]);
    this.messagesCache.clear();
  }

  // API calls
  loadChatHistory(patientId: number, destroy$: Subject<void>): void {
    const cacheKey = `history-${patientId}`;
    
    // Check cache first
    if (this.messagesCache.has(cacheKey)) {
      this.messages$.next(this.messagesCache.get(cacheKey) || []);
      return;
    }

    this.loading$.next(true);
    this.http.get<any[]>(`${this.API_BASE}/ai/history/${patientId}`)
      .pipe(takeUntil(destroy$))
      .subscribe({
        next: (history) => {
          const messages = history.slice(0, this.MAX_MESSAGES).map((h, i) => ({
            id: `history-${i}`,
            role: 'assistant' as const,
            content: h.answer,
            timestamp: new Date(h.createdAt),
            patientAdvice: h.patientAdvice,
            doctorSummary: h.doctorSummary,
            riskLevel: h.riskLevel,
            redFlags: h.redFlags,
            homeRemedies: h.homeRemedies,
            specializationHint: h.specializationHint
          }));
          
          this.messagesCache.set(cacheKey, messages);
          this.messages$.next(messages);
        },
        error: (error) => {
          console.error('Failed to load history:', error);
          this.error$.next('Failed to load chat history');
        },
        complete: () => {
          this.loading$.next(false);
        }
      });
  }

  sendMessage(
    patientId: number,
    question: string,
    context: ConversationContext,
    destroy$: Subject<void>
  ): Observable<any> {
    this.typing$.next(true);
    this.error$.next(null);

    const body = {
      patientId,
      question,
      conversationId: context.conversationId,
      sessionId: context.sessionId,
      messageOrder: context.messageOrder
    };

    return this.http.post<any>(`${this.API_BASE}/ai/consult`, body)
      .pipe(takeUntil(destroy$));
  }

  sendFeedback(
    messageId: string,
    type: 'positive' | 'negative',
    patientId: number,
    destroy$: Subject<void>
  ): void {
    const feedback = {
      messageId,
      type,
      patientId,
      timestamp: new Date().toISOString()
    };

    this.http.post(`${this.API_BASE}/ai/feedback`, feedback)
      .pipe(takeUntil(destroy$))
      .subscribe({
        error: (error) => console.warn('Failed to send feedback:', error)
      });
  }

  // Utility methods
  generateMessageId(): string {
    return `msg_${Date.now()}_${Math.random().toString(36).substr(2, 9)}`;
  }

  extractRemedies(answer: string): string[] {
    if (!answer) return [];
    return answer
      .split(/\.|\n|\r/)
      .map(s => s.trim())
      .filter(s => s.length > 0)
      .slice(0, 6);
  }

  setLoading(loading: boolean): void {
    this.loading$.next(loading);
  }

  setTyping(typing: boolean): void {
    this.typing$.next(typing);
  }

  setError(error: string | null): void {
    this.error$.next(error);
  }

  clearError(): void {
    this.error$.next(null);
  }

  // Export conversation
  exportConversation(patientId: number, conversationId: string | null): void {
    const conversation = {
      patientId,
      conversationId,
      messages: this.messages$.value,
      exportedAt: new Date().toISOString()
    };
    
    const blob = new Blob([JSON.stringify(conversation, null, 2)], { type: 'application/json' });
    const url = URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = `ai-conversation-${new Date().toISOString().split('T')[0]}.json`;
    a.click();
    URL.revokeObjectURL(url);
  }

  // Copy functionality
  async copyToClipboard(text: string): Promise<boolean> {
    try {
      if (navigator.clipboard) {
        await navigator.clipboard.writeText(text);
        return true;
      } else {
        // Fallback for older browsers
        const textArea = document.createElement('textarea');
        textArea.value = text;
        document.body.appendChild(textArea);
        textArea.select();
        document.execCommand('copy');
        document.body.removeChild(textArea);
        return true;
      }
    } catch (err) {
      console.error('Failed to copy text:', err);
      return false;
    }
  }

  // Toast notification helper
  showToast(message: string): void {
    const toast = document.createElement('div');
    toast.textContent = message;
    toast.style.cssText = `
      position: fixed; top: 20px; right: 20px; background: #4CAF50; 
      color: white; padding: 12px 24px; border-radius: 4px; 
      z-index: 10000; transition: opacity 0.3s;
    `;
    document.body.appendChild(toast);
    setTimeout(() => {
      toast.style.opacity = '0';
      setTimeout(() => document.body.removeChild(toast), 300);
    }, 2000);
  }
}
