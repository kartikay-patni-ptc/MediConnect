export interface Prescription {
  id?: number;
  prescriptionNumber?: string;
  appointment?: any;
  doctor?: any;
  patient?: any;
  diagnosis: string;
  symptoms: string;
  doctorNotes: string;
  prescriptionImageUrl?: string;
  originalFileName?: string;
  status: PrescriptionStatus;
  issuedDate?: Date;
  validUntil?: Date;
  medicines: PrescriptionMedicine[];
  orders?: MedicineOrder[];
  createdAt?: Date;
  updatedAt?: Date;
}

export interface PrescriptionMedicine {
  id?: number;
  medicineName: string;
  genericName: string;
  dosage: string;
  frequency: string;
  duration: string;
  quantity: number;
  instructions: string;
  medicineType: MedicineType;
  specialInstructions?: string;
  sideEffects?: string;
  contraindications?: string;
}

export interface MedicineOrder {
  id?: number;
  orderNumber?: string;
  prescription?: Prescription;
  patient?: any;
  pharmacy?: any;
  status: OrderStatus;
  orderType: OrderType;
  totalAmount: number;
  deliveryFee: number;
  finalAmount: number;
  deliveryAddress: string;
  deliveryPincode: string;
  patientPhoneNumber?: string;
  specialInstructions?: string;
  pharmacyNotes?: string;
  rejectionReason?: string;
  orderItems: OrderItem[];
  deliveryTracking?: DeliveryTracking;
  payment?: OrderPayment;
  createdAt?: Date;
  updatedAt?: Date;
  acceptedAt?: Date;
  expectedDeliveryTime?: Date;
}

export interface OrderItem {
  id?: number;
  prescriptionMedicine?: PrescriptionMedicine;
  medicineName: string;
  dosage: string;
  quantityRequested: number;
  quantityAvailable?: number;
  quantityProvided?: number;
  unitPrice?: number;
  totalPrice?: number;
  brandName?: string;
  manufacturerName?: string;
  batchNumber?: string;
  expiryDate?: string;
  status: ItemStatus;
  substitutionNote?: string;
  unavailabilityReason?: string;
}

export interface DeliveryTracking {
  id?: number;
  trackingNumber?: string;
  deliveryStatus: DeliveryStatus;
  deliveryPartnerName?: string;
  deliveryPartnerPhone?: string;
  vehicleNumber?: string;
  currentLatitude?: number;
  currentLongitude?: number;
  estimatedDistance?: number;
  estimatedTimeMinutes?: number;
  pickupTime?: Date;
  deliveryTime?: Date;
  estimatedDeliveryTime?: Date;
  deliveryInstructions?: string;
  deliveryNotes?: string;
  deliveryProofImageUrl?: string;
  recipientName?: string;
  recipientSignature?: string;
  deliveryUpdates?: DeliveryUpdate[];
}

export interface DeliveryUpdate {
  id?: number;
  updateType: UpdateType;
  title: string;
  description: string;
  latitude?: number;
  longitude?: number;
  location?: string;
  timestamp: Date;
  updatedBy?: string;
}

export interface OrderPayment {
  id?: number;
  paymentId?: string;
  paymentMethod: PaymentMethod;
  paymentStatus: PaymentStatus;
  amount: number;
  refundAmount?: number;
  transactionId?: string;
  gatewayResponse?: string;
  failureReason?: string;
  refundId?: string;
  refundReason?: string;
  createdAt?: Date;
  paidAt?: Date;
  refundedAt?: Date;
}



// Enums
export enum PrescriptionStatus {
  ACTIVE = 'ACTIVE',
  EXPIRED = 'EXPIRED',
  CANCELLED = 'CANCELLED',
  COMPLETED = 'COMPLETED'
}

export enum MedicineType {
  PRESCRIPTION = 'PRESCRIPTION',
  OTC = 'OTC',
  CONTROLLED = 'CONTROLLED'
}

export enum OrderStatus {
  PENDING = 'PENDING',
  PHARMACY_ASSIGNED = 'PHARMACY_ASSIGNED',
  ACCEPTED = 'ACCEPTED',
  REJECTED = 'REJECTED',
  PREPARING = 'PREPARING',
  READY_FOR_PICKUP = 'READY_FOR_PICKUP',
  OUT_FOR_DELIVERY = 'OUT_FOR_DELIVERY',
  DELIVERED = 'DELIVERED',
  CANCELLED = 'CANCELLED',
  REFUNDED = 'REFUNDED'
}

export enum OrderType {
  DELIVERY = 'DELIVERY',
  PICKUP = 'PICKUP'
}

export enum ItemStatus {
  PENDING = 'PENDING',
  AVAILABLE = 'AVAILABLE',
  PARTIALLY_AVAILABLE = 'PARTIALLY_AVAILABLE',
  SUBSTITUTED = 'SUBSTITUTED',
  UNAVAILABLE = 'UNAVAILABLE'
}

export enum DeliveryStatus {
  PENDING = 'PENDING',
  ASSIGNED = 'ASSIGNED',
  PICKED_UP = 'PICKED_UP',
  IN_TRANSIT = 'IN_TRANSIT',
  NEARBY = 'NEARBY',
  DELIVERED = 'DELIVERED',
  FAILED_DELIVERY = 'FAILED_DELIVERY',
  RETURNED_TO_PHARMACY = 'RETURNED_TO_PHARMACY',
  CANCELLED = 'CANCELLED'
}

export enum UpdateType {
  STATUS_CHANGE = 'STATUS_CHANGE',
  LOCATION_UPDATE = 'LOCATION_UPDATE',
  DELAY_NOTIFICATION = 'DELAY_NOTIFICATION',
  SPECIAL_NOTE = 'SPECIAL_NOTE',
  DELIVERY_ATTEMPT = 'DELIVERY_ATTEMPT',
  CUSTOMER_CONTACT = 'CUSTOMER_CONTACT'
}

export enum PaymentMethod {
  CREDIT_CARD = 'CREDIT_CARD',
  DEBIT_CARD = 'DEBIT_CARD',
  UPI = 'UPI',
  NET_BANKING = 'NET_BANKING',
  DIGITAL_WALLET = 'DIGITAL_WALLET',
  CASH_ON_DELIVERY = 'CASH_ON_DELIVERY'
}

export enum PaymentStatus {
  PENDING = 'PENDING',
  PROCESSING = 'PROCESSING',
  COMPLETED = 'COMPLETED',
  FAILED = 'FAILED',
  CANCELLED = 'CANCELLED',
  REFUNDED = 'REFUNDED',
  PARTIALLY_REFUNDED = 'PARTIALLY_REFUNDED'
}

// Helper interfaces for API requests
export interface CreatePrescriptionRequest {
  appointmentId?: number;
  diagnosis: string;
  symptoms: string;
  doctorNotes: string;
  medicines: PrescriptionMedicine[];
}

export interface CreateOrderRequest {
  prescriptionId: number;
  patientId: number;
  deliveryAddress: string;
  deliveryPincode: string;
  specialInstructions?: string;
}

export interface DeliveryEstimate {
  distance: number;
  estimatedTimeMinutes: number;
  deliveryFee: number;
}

export interface PharmacyStore {
  id: number;
  name: string;
  address: string;
  pincode: string;
  phone: string;
  email?: string;
  rating?: number;
  distance: number;
  deliveryTime?: string;
  isOpen: boolean;
  coordinates?: {
    latitude: number;
    longitude: number;
  };
}
