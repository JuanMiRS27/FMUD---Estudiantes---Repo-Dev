export type StudentStatus = 'ACTIVE' | 'INACTIVE';
export type DocumentStatus = 'ACTIVE' | 'REPLACED' | 'DELETED';
export type EnrollmentStatus = 'ENROLLED' | 'WITHDRAWN' | 'COMPLETED';

export interface Student {
  id: string;
  firstName: string;
  lastName: string;
  documentNumber: string;
  birthDate: string;
  birthPlace: string | null;
  address: string | null;
  phone: string | null;
  email: string | null;
  photoUrl: string | null;
  status: StudentStatus;
  createdAt: string;
  updatedAt: string;
}

export interface ResumeDetails {
  personal: PersonalInfo;
  socioeconomic: SocioeconomicInfo;
  academic: AcademicInfo;
  motivation: MotivationInfo;
  availability: AvailabilityInfo;
  foundationKnowledge: FoundationKnowledgeInfo;
  authorizations: AuthorizationsInfo;
  health: HealthInfo;
  riskFactors: RiskFactorsInfo;
  academicPerformance: AcademicPerformanceInfo;
  programKnowledge: ProgramKnowledgeInfo;
  institutionalCommitment: InstitutionalCommitmentInfo;
  declaration: DeclarationInfo;
}

export interface PersonalInfo {
  documentType: string | null;
  municipality: string | null;
  department: string | null;
  civilStatus: string | null;
  hasChildren: boolean | null;
  childrenCount: number | null;
  emergencyContactName: string | null;
  emergencyContactPhone: string | null;
}

export interface SocioeconomicInfo {
  livesWith: string | null;
  familyMembersCount: number | null;
  mainHouseholdProvider: string | null;
  monthlyFamilyIncome: string | null;
  housingType: string | null;
  healthSystemAffiliation: string | null;
  healthRegime: string | null;
  currentlyWorks: boolean | null;
  company: string | null;
  position: string | null;
  workSchedule: string | null;
  unemployedLastSixMonths: boolean | null;
  receivesGovernmentAid: boolean | null;
}

export interface AcademicInfo {
  lastApprovedLevel: string | null;
  institution: string | null;
  graduationYear: number | null;
  higherStudies: string | null;
  previousScholarships: string | null;
  complementaryCertificates: string | null;
  internetAccess: boolean | null;
  studyDeviceAvailability: boolean | null;
}

export interface MotivationInfo {
  scholarshipReason: string | null;
  programMotivation: string | null;
  personalProfessionalGoals: string | null;
  qualityOfLifeImpact: string | null;
  expectedLearning: string | null;
  knowledgeApplicationPlan: string | null;
}

export interface AvailabilityInfo {
  availableForClasses: boolean | null;
  timeLimitations: boolean | null;
  timeLimitationsDescription: string | null;
  acceptsInstitutionRules: boolean | null;
  participatesCommunityActivities: boolean | null;
  attendanceCommitment: boolean | null;
}

export interface FoundationKnowledgeInfo {
  callSource: string[];
  knewFoundationBefore: boolean | null;
  knownSocialWork: string | null;
}

export interface AuthorizationsInfo {
  personalDataProcessing: boolean | null;
  informationVerification: boolean | null;
  photoVideoUse: boolean | null;
}

export interface HealthInfo {
  diagnosedDisease: boolean | null;
  diagnosedDiseaseName: string | null;
  chronicDisease: boolean | null;
  chronicDiseaseName: string | null;
  physicalLimitation: boolean | null;
  physicalLimitationDescription: string | null;
  disability: boolean | null;
  disabilityDescription: string | null;
  allergies: boolean | null;
  allergiesSpecification: string | null;
  currentMedicalTreatment: boolean | null;
  currentMedicalTreatmentName: string | null;
  permanentMedication: boolean | null;
  permanentMedicationNames: string | null;
  hospitalizedLastTwoYears: boolean | null;
  hospitalizationReason: string | null;
  completeVaccination: boolean | null;
  vaccines: string[];
  visualDifficulties: boolean | null;
  usesGlasses: boolean | null;
  hearingDifficulties: boolean | null;
  accidentsWithSequelae: boolean | null;
  accidentExplanation: string | null;
  activeHealthAffiliation: boolean | null;
  eps: string | null;
  medicalEmergencyContactName: string | null;
  medicalEmergencyContactRelationship: string | null;
  medicalEmergencyContactPhone: string | null;
}

export interface RiskFactorsInfo {
  tobaccoUse: string | null;
  alcoholUse: string | null;
  psychoactiveSubstancesUse: boolean | null;
  substanceUseTime: string | null;
  consumptionAffectedPerformance: boolean | null;
  preventionProgramParticipation: boolean | null;
  currentProfessionalSupport: boolean | null;
  preventionGuidanceInterest: boolean | null;
  healthyHabitsActivitiesWillingness: boolean | null;
  personalFamilySituationAffectsProcess: boolean | null;
  personalFamilySituationDescription: string | null;
}

export interface AcademicPerformanceInfo {
  bestSubject: string | null;
  strengthAreas: string[];
  academicSkills: string[];
  academicRecognitions: boolean | null;
  academicRecognitionsDetails: string | null;
  subjectsToStrengthen: string | null;
  studyHabits: string | null;
  weeklyStudyHours: string | null;
  taskResponsibility: string | null;
  mainAcademicAchievement: string | null;
  areasToStrengthen: string | null;
}

export interface ProgramKnowledgeInfo {
  nursingUnderstanding: string | null;
  geriatricsUnderstanding: string | null;
  gerontologyUnderstanding: string | null;
  geriatricsGerontologyDifference: string | null;
  studyReason: string | null;
  careExperience: boolean | null;
  careExperienceDescription: string | null;
  elderCareQualities: string | null;
  humanDignityMeaning: string | null;
  sadOlderAdultAction: string | null;
  needsHelpNoStaffAction: string | null;
  patienceImportance: string | null;
  healthValues: string | null;
  programExpectedLearning: string | null;
  communityContribution: string | null;
  desiredWorkplace: string | null;
  humanizedServiceMeaning: string | null;
  practiceResponsibility: boolean | null;
  mainTrainingChallenge: string | null;
  refusesHelpReaction: string | null;
  scholarshipMeritReason: string | null;
}

export interface InstitutionalCommitmentInfo {
  foundationSocialWorkKnowledge: boolean | null;
  foundationSocialWorkDescription: string | null;
  callSource: string[];
  foundationProgramReason: string | null;
  understandsScholarshipResponsibility: boolean | null;
  studentRulesCommitment: boolean | null;
  respectfulConductCommitment: boolean | null;
  punctualAttendanceCommitment: boolean | null;
  socialCommunityParticipation: boolean | null;
  resourceCareCommitment: boolean | null;
  understandsNonComplianceConsequences: boolean | null;
  trackingAuthorization: boolean | null;
}

export interface DeclarationInfo {
  truthfulCompleteInformation: boolean | null;
  applicantName: string | null;
  identityDocument: string | null;
  signatureDate: string | null;
}

export interface PageResponse<T> {
  content: T[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
}

export interface StudentDocument {
  id: string;
  studentId: string;
  documentType: string;
  displayName: string;
  originalName: string;
  contentType: string;
  size: number;
  description: string | null;
  status: DocumentStatus;
  uploadedByUserId: string;
  createdAt: string;
  updatedAt: string;
}

export interface Enrollment {
  id: string;
  studentId: string;
  periodCode: string;
  program: string;
  status: EnrollmentStatus;
  createdAt: string;
  updatedAt: string;
}

export interface HistoryEvent {
  id: string;
  studentId: string;
  documentId: string | null;
  actorUserId: string;
  action: string;
  summary: string;
  createdAt: string;
}

export interface Resume {
  student: Student;
  details: ResumeDetails;
  enrollments: Enrollment[];
  documents: StudentDocument[];
  history: HistoryEvent[];
}

export interface StudentFormValue {
  firstName: string;
  lastName: string;
  documentNumber: string;
  birthDate: string;
  birthPlace: string;
  address: string;
  phone: string;
  email: string;
  status: StudentStatus;
}

export interface EnrollmentFormValue {
  periodCode: string;
  program: string;
  status: EnrollmentStatus;
}
