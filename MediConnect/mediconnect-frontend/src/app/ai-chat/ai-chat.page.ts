import { Component, ElementRef, ViewChild, OnDestroy, ChangeDetectorRef, OnInit } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { AuthService } from '../auth/auth.service';
import { BookAppointmentDialogComponent } from '../appointment/book-appointment-dialog.component';
import { AppointmentService } from '../appointment/appointment.service';
import { Doctor } from '../appointment/appointment.model';
import { AiChatService, AiChatMessage, ConversationContext } from './ai-chat.service';
import { Subject, debounceTime, distinctUntilChanged, takeUntil, BehaviorSubject, Observable } from 'rxjs';

interface ChatMessage { 
  role: 'user' | 'assistant'; 
  content: string; 
  timestamp: Date;
  id?: string;
  // Enhanced AI fields
  patientAdvice?: string;
  doctorSummary?: any;
  prescribedMedicines?: { name: string; dose: string; frequency: string; duration: string; otcOrPrescription: string; }[];
  riskLevel?: string;
  redFlags?: string[];
  homeRemedies?: string[];
  specializationHint?: string;
}

interface RichMessage extends ChatMessage { 
  id: string; 
  liked?: boolean; 
  disliked?: boolean; 
  isTyping?: boolean;
  error?: boolean;
}

@Component({
	selector: 'app-ai-chat-page',
	templateUrl: './ai-chat.page.html',
	styleUrls: ['./ai-chat.page.css']
})
export class AiChatPageComponent implements OnInit, OnDestroy {
	// Core chat state
	input = '';
	messages: RichMessage[] = [];
	isLoading = false;
	remedies: string[] = [];
	suggestedDoctors: any[] = [];
	lastQuestion: string | null = null;
	showBookingDialog = false;
	selectedDoctor: Doctor | null = null;
	patientId: number = 0;
	aiSummary: string | null = null;
	currentUser: any;
	userRole: string = '';

	// Enhanced state management
	private destroy$ = new Subject<void>();
	private inputSubject = new Subject<string>();
	private conversationId: string | null = null;
	private sessionId: string | null = null;
	private messageOrder = 1;
	private readonly API_BASE = 'http://localhost:8080/api';

	// Performance optimization
	private messagesCache = new Map<string, RichMessage[]>();
	loadingState$ = new BehaviorSubject<boolean>(false);
	typingIndicator$ = new BehaviorSubject<boolean>(false);
	errorMessage$ = new BehaviorSubject<string | null>(null);

	// Enhanced quick prompts with categories
	quickPrompts = {
		common: [
			"I have a cold and mild fever",
			"I have a headache",
			"I feel stomach acidity",
			"I have a skin rash",
			"I feel chest discomfort"
		],
		emergency: [
			"Severe chest pain",
			"Difficulty breathing",
			"Sudden severe headache",
			"High fever with confusion"
		],
		chronic: [
			"Managing diabetes",
			"Hypertension concerns",
			"Arthritis pain management",
			"Mental health support"
		]
	};

	// History management
	history: string[] = [];
	private readonly MAX_HISTORY = 10;
	private readonly MAX_MESSAGES = 100; // Limit messages for performance

	@ViewChild('messagesEl') messagesEl?: ElementRef<HTMLDivElement>;
	@ViewChild('inputEl') inputEl?: ElementRef<HTMLTextAreaElement>;

	constructor(
		private http: HttpClient, 
		private auth: AuthService, 
		private appointmentService: AppointmentService,
		private aiChatService: AiChatService,
		private cdr: ChangeDetectorRef
	) {
		this.patientId = this.auth.getUserId();
		this.setupInputDebouncing();
	}

	ngOnInit() {
		this.currentUser = this.auth.getCurrentUser();
		this.userRole = this.currentUser?.role || '';
		this.loadHistory();
		this.setupErrorHandling();
		this.loadConversationContext();
		this.loadStoredHistory();
	}

	ngOnDestroy() {
		this.destroy$.next();
		this.destroy$.complete();
		this.saveConversationContext();
	}

	private setupInputDebouncing() {
		this.inputSubject.pipe(
			takeUntil(this.destroy$),
			debounceTime(300),
			distinctUntilChanged()
		).subscribe(() => {
			// Add typing indicator logic here if needed
			this.typingIndicator$.next(false);
		});
	}

	private setupErrorHandling() {
		// Global error handling for the component
		this.errorMessage$.pipe(
			takeUntil(this.destroy$)
		).subscribe(error => {
			if (error) {
				setTimeout(() => this.errorMessage$.next(null), 5000);
			}
		});
	}

	private loadConversationContext() {
		const stored = localStorage.getItem(`ai-chat-context-${this.patientId}`);
		if (stored) {
			try {
				const context = JSON.parse(stored);
				// Check if context is recent (within 1 hour)
				const timeDiff = Date.now() - (context.timestamp || 0);
				if (timeDiff < 60 * 60 * 1000) { // 1 hour
					this.conversationId = context.conversationId;
					this.sessionId = context.sessionId;
					this.messageOrder = context.messageOrder || 1;
				}
			} catch (e) {
				console.warn('Failed to load conversation context:', e);
			}
		}
	}

	private loadStoredHistory() {
		const stored = localStorage.getItem(`ai-chat-history-${this.patientId}`);
		if (stored) {
			try {
				this.history = JSON.parse(stored);
			} catch (e) {
				console.warn('Failed to load history:', e);
			}
		}
	}

	private saveConversationContext() {
		const context = {
			conversationId: this.conversationId,
			sessionId: this.sessionId,
			messageOrder: this.messageOrder,
			timestamp: Date.now()
		};
		localStorage.setItem(`ai-chat-context-${this.patientId}`, JSON.stringify(context));
	}

	loadHistory() {
		const cacheKey = `history-${this.patientId}`;
		
		// Check cache first
		if (this.messagesCache.has(cacheKey)) {
			this.messages = this.messagesCache.get(cacheKey) || [];
			this.scheduleScrollToBottom();
			return;
		}

		this.loadingState$.next(true);
		this.http.get<any[]>(`${this.API_BASE}/ai/history/${this.patientId}`)
			.pipe(takeUntil(this.destroy$))
			.subscribe({
				next: (history: any[]) => {
					this.messages = history.slice(0, this.MAX_MESSAGES).map((h: any, i: number) => ({
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
					
					// Cache the results
					this.messagesCache.set(cacheKey, this.messages);
					this.scheduleScrollToBottom();
				},
				error: (error: any) => {
					console.error('Failed to load history:', error);
					this.errorMessage$.next('Failed to load chat history');
				},
				complete: () => {
					this.loadingState$.next(false);
				}
			});
	}

	send() {
		const content = this.input.trim();
		if (!content) return;

		// Validate input length
		if (content.length > 1000) {
			this.errorMessage$.next('Message too long. Please keep it under 1000 characters.');
			return;
		}

		// Clear any existing errors
		this.errorMessage$.next(null);

		// Add to history
		this.lastQuestion = content;
		this.addToHistory(content);

		// Create user message with unique ID
		const messageId = this.generateMessageId();
		const userMessage: RichMessage = {
			id: messageId,
			role: 'user',
			content,
			timestamp: new Date()
		};

		// Add user message immediately
		this.addMessage(userMessage);
		this.input = '';

		// Show typing indicator
		this.isLoading = true;
		this.typingIndicator$.next(true);

		// Prepare request with conversation context
		const body = {
			patientId: this.patientId,
			question: content,
			conversationId: this.conversationId,
			sessionId: this.sessionId,
			messageOrder: this.messageOrder
		};

		// Send request with retry logic
		this.sendWithRetry(body, 0);
	}

	private sendWithRetry(body: any, attempt: number) {
		const maxRetries = 3;
		
		this.http.post<any>(`${this.API_BASE}/ai/consult`, body)
			.pipe(takeUntil(this.destroy$))
			.subscribe({
				next: (response: any) => this.handleAIResponse(response),
				error: (error: any) => {
					console.error('AI consultation error:', error);
					if (attempt < maxRetries) {
						// Retry with exponential backoff
						const delay = Math.pow(2, attempt) * 1000;
						setTimeout(() => this.sendWithRetry(body, attempt + 1), delay);
					} else {
						this.handleAIError(error);
					}
				}
			});
	}

	private handleAIResponse(response: any) {
		// Update conversation context
		this.conversationId = response.conversationId || this.conversationId;
		this.sessionId = response.sessionId || this.sessionId;
		this.messageOrder = response.messageOrder || this.messageOrder + 1;

		// Create AI message with full response data
		const aiMessage: RichMessage = {
			id: this.generateMessageId(),
			role: 'assistant',
			content: response.patientAdvice || response.answer || 'No response received',
			timestamp: new Date(),
			patientAdvice: response.patientAdvice,
			doctorSummary: response.doctorSummary,
			prescribedMedicines: response.prescribedMedicines,
			riskLevel: response.riskLevel,
			redFlags: response.redFlags,
			homeRemedies: response.homeRemedies,
			specializationHint: response.specializationHint
		};

		// Add AI message
		this.addMessage(aiMessage);

		// Update side panel data
		this.remedies = response.homeRemedies || this.extractRemedies(response.answer || '');
		this.suggestedDoctors = Array.isArray(response.specialists) ? response.specialists.slice(0, 6) : [];

		// Save context and cleanup
		this.saveConversationContext();
		this.cleanupState();
	}

	private handleAIError(error: any) {
		const errorMessage: RichMessage = {
			id: this.generateMessageId(),
			role: 'assistant',
			content: 'I apologize, but I\'m having trouble responding right now. Please try again in a moment.',
			timestamp: new Date(),
			error: true
		};

		this.addMessage(errorMessage);
		this.errorMessage$.next('Failed to get AI response. Please try again.');
		this.cleanupState();
	}

	private cleanupState() {
		this.isLoading = false;
		this.typingIndicator$.next(false);
		this.cdr.detectChanges();
	}

	ask(prompt: string) {
		this.input = prompt;
		this.inputSubject.next(prompt);
		// Auto-focus input after setting prompt
		setTimeout(() => {
			if (this.inputEl?.nativeElement) {
				this.inputEl.nativeElement.focus();
				this.inputEl.nativeElement.setSelectionRange(prompt.length, prompt.length);
			}
		}, 100);
		this.send();
	}

	regenerate() {
		if (this.lastQuestion) {
			this.input = this.lastQuestion;
			this.send();
		}
	}

	async copy(content: string) {
		const success = await this.aiChatService.copyToClipboard(content);
		if (success) {
			this.aiChatService.showToast('Copied to clipboard!');
		} else {
			this.errorMessage$.next('Failed to copy text');
		}
	}

	vote(msg: RichMessage, up: boolean) {
		if (up) {
			msg.liked = !msg.liked;
			msg.disliked = false;
		} else {
			msg.disliked = !msg.disliked;
			msg.liked = false;
		}

		// Send feedback to backend (optional)
		this.sendFeedback(msg.id, up ? 'positive' : 'negative', up ? msg.liked : msg.disliked);
	}

	private sendFeedback(messageId: string, type: 'positive' | 'negative', active: boolean) {
		if (!active) return; // Only send when feedback is given, not when removed
		this.aiChatService.sendFeedback(messageId, type, this.patientId, this.destroy$);
	}

	onKeydown(event: KeyboardEvent) {
		if (event.key === 'Enter' && !event.shiftKey && !this.isLoading) {
			event.preventDefault();
			this.send();
		} else if (event.key === 'Escape') {
			// Clear input on Escape
			this.input = '';
		}
	}

	onInput(event: any) {
		this.inputSubject.next(event.target.value);
		// Auto-resize textarea
		const textarea = event.target;
		textarea.style.height = 'auto';
		textarea.style.height = Math.min(textarea.scrollHeight, 120) + 'px';
	}

	private extractRemedies(answer: string): string[] {
		if (!answer) return [];
		return answer
			.split(/\.|\n|\r/)
			.map(s => s.trim())
			.filter(s => s.length > 0)
			.slice(0, 6);
	}

	private scheduleScrollToBottom() {
		// Use requestAnimationFrame for smooth scrolling
		requestAnimationFrame(() => {
			if (this.messagesEl?.nativeElement) {
				const el = this.messagesEl.nativeElement;
				el.scrollTo({
					top: el.scrollHeight,
					behavior: 'smooth'
				});
			}
		});
	}

	private scrollToBottom() {
		if (this.messagesEl?.nativeElement) {
			const el = this.messagesEl.nativeElement;
			el.scrollTop = el.scrollHeight;
		}
	}

	// Additional properties for appointment booking dialog
	doctorSummary: string | null = null;
	patientAdvice: string | null = null;
	prescribedMedicines: string | null = null;
	riskLevel: string | null = null;
	redFlags: string | null = null;
	homeRemedies: string | null = null;
	specializationHint: string | null = null;

	openBookingDialogFromAI(doctor: Doctor) {
		this.selectedDoctor = doctor;
		this.showBookingDialog = true;
		// Use the last assistant message as AI summary with enhanced data
		const lastAssistant = this.messages.filter(m => m.role === 'assistant').slice(-1)[0];
		if (lastAssistant) {
			// Set individual properties for the booking dialog
			this.patientAdvice = lastAssistant.patientAdvice || lastAssistant.content;
			this.doctorSummary = typeof lastAssistant.doctorSummary === 'string' 
				? lastAssistant.doctorSummary 
				: (lastAssistant.doctorSummary ? JSON.stringify(lastAssistant.doctorSummary) : null);
			this.riskLevel = lastAssistant.riskLevel || null;
			this.redFlags = lastAssistant.redFlags ? JSON.stringify(lastAssistant.redFlags) : null;
			this.prescribedMedicines = lastAssistant.prescribedMedicines ? JSON.stringify(lastAssistant.prescribedMedicines) : null;
			this.homeRemedies = lastAssistant.homeRemedies ? JSON.stringify(lastAssistant.homeRemedies) : null;
			this.specializationHint = lastAssistant.specializationHint || null;
			
			// Keep the old aiSummary for backward compatibility
			this.aiSummary = this.patientAdvice;
		}
	}

	onAppointmentBooked(appointment: any) {
		this.aiChatService.showToast('Appointment booked successfully!');
		this.showBookingDialog = false;
		
		// Add appointment confirmation message to chat
		const confirmationMessage: RichMessage = {
			id: this.generateMessageId(),
			role: 'assistant',
			content: `Great! Your appointment with Dr. ${appointment.doctorName} has been booked for ${new Date(appointment.appointmentDate).toLocaleDateString()}.`,
			timestamp: new Date()
		};
		this.addMessage(confirmationMessage);
	}

	// Utility methods
	private generateMessageId(): string {
		return `msg_${Date.now()}_${Math.random().toString(36).substr(2, 9)}`;
	}

	private addMessage(message: RichMessage) {
		this.messages.push(message);
		
		// Limit messages for performance
		if (this.messages.length > this.MAX_MESSAGES) {
			this.messages = this.messages.slice(-this.MAX_MESSAGES);
		}
		
		// Clear cache when new messages are added
		this.messagesCache.clear();
		
		this.scheduleScrollToBottom();
		this.cdr.detectChanges();
	}

	private addToHistory(question: string) {
		this.history.unshift(question);
		this.history = this.history.slice(0, this.MAX_HISTORY);
		
		// Save to localStorage
		localStorage.setItem(`ai-chat-history-${this.patientId}`, JSON.stringify(this.history));
	}

	// New conversation management
	startNewConversation() {
		this.conversationId = null;
		this.sessionId = null;
		this.messageOrder = 1;
		this.messages = [];
		this.messagesCache.clear();
		this.remedies = [];
		this.suggestedDoctors = [];
		this.input = '';
		this.errorMessage$.next(null);
		
		// Clear stored context
		localStorage.removeItem(`ai-chat-context-${this.patientId}`);
		
		this.aiChatService.showToast('New conversation started');
	}

	// Accessibility and user experience methods
	focusInput() {
		if (this.inputEl?.nativeElement) {
			this.inputEl.nativeElement.focus();
		}
	}

	getQuickPromptsByCategory(category: keyof typeof this.quickPrompts): string[] {
		return this.quickPrompts[category] || [];
	}

	getMessageCount(): number {
		return this.messages.length;
	}

	isInputDisabled(): boolean {
		return this.isLoading || this.input.length > 1000;
	}

	// Enhanced error handling
	getErrorMessage(): Observable<string | null> {
		return this.errorMessage$.asObservable();
	}

	getLoadingState(): Observable<boolean> {
		return this.loadingState$.asObservable();
	}

	dismissError() {
		this.errorMessage$.next(null);
	}

	// Export conversation (useful feature)
	exportConversation() {
		this.aiChatService.exportConversation(this.patientId, this.conversationId);
		this.aiChatService.showToast('Conversation exported!');
	}

	// Performance optimization: TrackBy function for ngFor
	trackMessage(index: number, message: RichMessage): string {
		return message.id;
	}
}