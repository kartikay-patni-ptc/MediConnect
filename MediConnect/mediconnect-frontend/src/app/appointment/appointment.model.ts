export interface Doctor {
  id: number;
  firstName: string;
  lastName: string;
  specialization: string;
  experience: number;
  hospital: string;
  phoneNumber: string;
  email: string;
}

export interface Patient {
  id: number;
  firstName: string;
  lastName: string;
  email: string;
  phoneNumber: string;
}

export interface DoctorSlot {
  id: number;
  doctorId: number;
  startTime: string; // ISO string
  endTime: string;   // ISO string
  available: boolean;
}

export interface Appointment {
  id?: number;
  patientId: number;
  doctorId: number;
  slotId: number;
  status: 'Pending' | 'Confirmed' | 'Cancelled' | 'Completed';
  notes?: string;
  aiSummary?: string;
  doctorSummary?: string;
  patientAdvice?: string;
  prescribedMedicines?: string;
  riskLevel?: string;
  redFlags?: string;
  homeRemedies?: string;
  specializationHint?: string;
  createdAt?: string;
  // Backend response fields
  patient?: Patient;
  doctor?: Doctor;
  slot?: DoctorSlot;
}

export interface AIConsultation {
  question: string;
  answer: string;
  riskLevel: 'low' | 'medium' | 'high';
  redFlags?: string[];
  specializationHint?: string;
  specialists?: Doctor[];
  aiUsed?: boolean;
  homeRemedies?: string[];
}

