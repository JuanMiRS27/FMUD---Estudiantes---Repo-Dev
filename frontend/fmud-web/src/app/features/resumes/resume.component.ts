import { DatePipe } from '@angular/common';
import { Component, computed, inject, signal } from '@angular/core';
import { AbstractControl, FormBuilder, FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { finalize, switchMap, tap } from 'rxjs';
import { AuthService } from '../../core/auth/auth.service';
import { ApiError } from '../../core/models/auth.model';
import { NotificationService } from '../../core/services/notification.service';
import { EmptyStateComponent } from '../../shared/components/empty-state/empty-state.component';
import { Enrollment, EnrollmentFormValue, Resume, ResumeDetails, StudentDocument, StudentFormValue, StudentStatus } from '../students/student.model';
import { StudentsService } from '../students/students.service';

type FieldType = 'text' | 'email' | 'number' | 'date' | 'select' | 'radio' | 'textarea' | 'checkboxes' | 'readonly';
type Field = {
  key: string;
  label: string;
  type: FieldType;
  options?: string[];
  required?: boolean;
  dependsOn?: string;
  showWhen?: string | string[];
  rows?: number;
};
type Section = { key: keyof ResumeDetails | 'student'; title: string; fields: Field[] };

const YES_NO = ['Si', 'No'];
const YES_NO_OCCASIONAL = ['Si', 'No', 'Ocasionalmente'];
const DOCUMENT_TYPES = [
  { value: 'IDENTITY_DOCUMENT', label: 'Documento de identidad' },
  { value: 'CIVIL_REGISTRY', label: 'Registro civil' },
  { value: 'STUDY_CERTIFICATE', label: 'Certificado de estudio' },
  { value: 'HEALTH_AFFILIATION', label: 'Afiliacion a salud' },
  { value: 'SIGNED_RESUME', label: 'Hoja de vida firmada' },
  { value: 'PHOTO', label: 'Fotografia' },
  { value: 'OTHER', label: 'Otro' }
];

const SECTIONS: Section[] = [
  {
    key: 'student',
    title: 'Informacion personal',
    fields: [
      { key: 'firstName', label: 'Nombres', type: 'text', required: true },
      { key: 'lastName', label: 'Apellidos', type: 'text', required: true },
      { key: 'documentType', label: 'Tipo de documento', type: 'select', options: ['Cedula de ciudadania', 'Tarjeta de identidad', 'Cedula de extranjeria', 'Pasaporte', 'Otro'] },
      { key: 'documentNumber', label: 'Numero de documento', type: 'text', required: true },
      { key: 'birthDate', label: 'Fecha de nacimiento', type: 'date', required: true },
      { key: 'age', label: 'Edad calculada', type: 'readonly' },
      { key: 'birthPlace', label: 'Lugar de nacimiento', type: 'text' },
      { key: 'address', label: 'Direccion de residencia', type: 'text' },
      { key: 'municipality', label: 'Municipio', type: 'text' },
      { key: 'department', label: 'Departamento', type: 'text' },
      { key: 'phone', label: 'Numero de telefono', type: 'text' },
      { key: 'email', label: 'Correo electronico', type: 'email' },
      { key: 'civilStatus', label: 'Estado civil', type: 'select', options: ['Soltero(a)', 'Casado(a)', 'Union libre', 'Separado(a)', 'Viudo(a)', 'Otro'] },
      { key: 'hasChildren', label: 'Tiene hijos', type: 'radio', options: YES_NO },
      { key: 'childrenCount', label: 'Cantidad de hijos', type: 'number', dependsOn: 'hasChildren', showWhen: 'Si' },
      { key: 'emergencyContactName', label: 'Persona de contacto en caso de emergencia', type: 'text' },
      { key: 'emergencyContactPhone', label: 'Telefono del contacto de emergencia', type: 'text' },
      { key: 'status', label: 'Estado', type: 'select', options: ['ACTIVE', 'INACTIVE'], required: true }
    ]
  },
  {
    key: 'socioeconomic',
    title: 'Informacion socioeconomica',
    fields: [
      { key: 'livesWith', label: 'Con quien vive actualmente', type: 'text' },
      { key: 'familyMembersCount', label: 'Numero de personas del nucleo familiar', type: 'number' },
      { key: 'mainHouseholdProvider', label: 'Principal sosten economico del hogar', type: 'text' },
      { key: 'monthlyFamilyIncome', label: 'Ingreso mensual aproximado de la familia', type: 'text' },
      { key: 'housingType', label: 'Tipo de vivienda', type: 'radio', options: ['Propia', 'Familiar', 'Arrendada', 'Prestada'] },
      { key: 'healthSystemAffiliation', label: 'Afiliacion al sistema de salud', type: 'text' },
      { key: 'healthRegime', label: 'Regimen', type: 'radio', options: ['Contributivo', 'Subsidiado', 'Especial'] },
      { key: 'currentlyWorks', label: 'Actualmente trabaja', type: 'radio', options: YES_NO },
      { key: 'company', label: 'Empresa', type: 'text', dependsOn: 'currentlyWorks', showWhen: 'Si' },
      { key: 'position', label: 'Cargo', type: 'text', dependsOn: 'currentlyWorks', showWhen: 'Si' },
      { key: 'workSchedule', label: 'Horario', type: 'text', dependsOn: 'currentlyWorks', showWhen: 'Si' },
      { key: 'unemployedLastSixMonths', label: 'Ha estado desempleado durante los ultimos seis meses', type: 'radio', options: YES_NO },
      { key: 'receivesGovernmentAid', label: 'Recibe subsidio o ayuda gubernamental', type: 'radio', options: YES_NO }
    ]
  },
  {
    key: 'academic',
    title: 'Informacion academica',
    fields: [
      { key: 'lastApprovedLevel', label: 'Ultimo nivel educativo aprobado', type: 'text' },
      { key: 'institution', label: 'Institucion donde curso sus estudios', type: 'text' },
      { key: 'graduationYear', label: 'Ano de graduacion', type: 'number' },
      { key: 'higherStudies', label: 'Estudios tecnicos, tecnologicos o universitarios', type: 'textarea' },
      { key: 'previousScholarships', label: 'Becas recibidas anteriormente', type: 'textarea' },
      { key: 'complementaryCertificates', label: 'Certificados de cursos complementarios', type: 'textarea' },
      { key: 'internetAccess', label: 'Acceso a Internet', type: 'radio', options: YES_NO },
      { key: 'studyDeviceAvailability', label: 'Disponibilidad de computador o dispositivo para estudio', type: 'radio', options: YES_NO }
    ]
  },
  {
    key: 'motivation',
    title: 'Motivacion y proyecto de vida',
    fields: [
      { key: 'scholarshipReason', label: 'Por que desea acceder a esta beca', type: 'textarea' },
      { key: 'programMotivation', label: 'Que lo motivo a elegir este programa', type: 'textarea' },
      { key: 'personalProfessionalGoals', label: 'Metas personales y profesionales', type: 'textarea' },
      { key: 'qualityOfLifeImpact', label: 'Como contribuira la beca a mejorar su calidad de vida', type: 'textarea' },
      { key: 'expectedLearning', label: 'Que espera aprender durante el programa', type: 'textarea' },
      { key: 'knowledgeApplicationPlan', label: 'Como planea aplicar los conocimientos adquiridos', type: 'textarea' }
    ]
  },
  {
    key: 'availability',
    title: 'Disponibilidad y compromiso',
    fields: [
      { key: 'availableForClasses', label: 'Disponibilidad para clases y actividades', type: 'radio', options: YES_NO },
      { key: 'timeLimitations', label: 'Limitaciones de tiempo por trabajo u otras responsabilidades', type: 'radio', options: YES_NO },
      { key: 'timeLimitationsDescription', label: 'Descripcion de limitaciones de tiempo', type: 'textarea', dependsOn: 'timeLimitations', showWhen: 'Si' },
      { key: 'acceptsInstitutionRules', label: 'Cumplimiento del reglamento institucional', type: 'radio', options: YES_NO },
      { key: 'participatesCommunityActivities', label: 'Participacion en actividades sociales o comunitarias', type: 'radio', options: YES_NO },
      { key: 'attendanceCommitment', label: 'Compromiso de asistencia y cumplimiento academico', type: 'radio', options: YES_NO }
    ]
  },
  {
    key: 'foundationKnowledge',
    title: 'Conocimiento de la Fundacion',
    fields: [
      { key: 'callSource', label: 'Como conocio la convocatoria', type: 'checkboxes', options: ['Redes sociales', 'Pagina web', 'Familiar o amigo', 'Institucion educativa', 'Otro'] },
      { key: 'knewFoundationBefore', label: 'Conocia previamente la Fundacion', type: 'radio', options: YES_NO },
      { key: 'knownSocialWork', label: 'Que conoce acerca de su labor social', type: 'textarea' }
    ]
  },
  {
    key: 'authorizations',
    title: 'Autorizaciones',
    fields: [
      { key: 'personalDataProcessing', label: 'Tratamiento de datos personales', type: 'radio', options: YES_NO, required: true },
      { key: 'informationVerification', label: 'Verificacion de la informacion suministrada', type: 'radio', options: YES_NO, required: true },
      { key: 'photoVideoUse', label: 'Uso de fotografias o material audiovisual para fines institucionales y educativos', type: 'radio', options: YES_NO, required: true }
    ]
  },
  {
    key: 'health',
    title: 'Informacion de salud del aspirante',
    fields: [
      { key: 'diagnosedDisease', label: 'Enfermedad diagnosticada', type: 'radio', options: YES_NO },
      { key: 'diagnosedDiseaseName', label: 'Cual enfermedad', type: 'text', dependsOn: 'diagnosedDisease', showWhen: 'Si' },
      { key: 'chronicDisease', label: 'Enfermedad cronica', type: 'radio', options: YES_NO },
      { key: 'chronicDiseaseName', label: 'Cual', type: 'text', dependsOn: 'chronicDisease', showWhen: 'Si' },
      { key: 'physicalLimitation', label: 'Limitacion fisica', type: 'radio', options: YES_NO },
      { key: 'physicalLimitationDescription', label: 'Descripcion de la limitacion', type: 'textarea', dependsOn: 'physicalLimitation', showWhen: 'Si' },
      { key: 'disability', label: 'Discapacidad', type: 'radio', options: YES_NO },
      { key: 'disabilityDescription', label: 'Descripcion', type: 'textarea', dependsOn: 'disability', showWhen: 'Si' },
      { key: 'allergies', label: 'Alergias', type: 'radio', options: YES_NO },
      { key: 'allergiesSpecification', label: 'Especificacion de alergias', type: 'textarea', dependsOn: 'allergies', showWhen: 'Si' },
      { key: 'currentMedicalTreatment', label: 'Tratamiento medico actual', type: 'radio', options: YES_NO },
      { key: 'currentMedicalTreatmentName', label: 'Cual tratamiento', type: 'textarea', dependsOn: 'currentMedicalTreatment', showWhen: 'Si' },
      { key: 'permanentMedication', label: 'Medicamentos permanentes', type: 'radio', options: YES_NO },
      { key: 'permanentMedicationNames', label: 'Cuales', type: 'textarea', dependsOn: 'permanentMedication', showWhen: 'Si' },
      { key: 'hospitalizedLastTwoYears', label: 'Hospitalizacion durante los ultimos dos anos', type: 'radio', options: YES_NO },
      { key: 'hospitalizationReason', label: 'Motivo', type: 'textarea', dependsOn: 'hospitalizedLastTwoYears', showWhen: 'Si' },
      { key: 'completeVaccination', label: 'Esquema de vacunacion completo', type: 'radio', options: YES_NO },
      { key: 'vaccines', label: 'Vacunas contra Hepatitis B, Tetanos e Influenza. Cuales', type: 'checkboxes', options: ['Hepatitis B', 'Tetanos', 'Influenza', 'Otra'] },
      { key: 'visualDifficulties', label: 'Dificultades visuales', type: 'radio', options: YES_NO },
      { key: 'usesGlasses', label: 'Uso de gafas o lentes', type: 'radio', options: YES_NO },
      { key: 'hearingDifficulties', label: 'Dificultades auditivas', type: 'radio', options: YES_NO },
      { key: 'accidentsWithSequelae', label: 'Accidentes que hayan dejado secuelas fisicas', type: 'radio', options: YES_NO },
      { key: 'accidentExplanation', label: 'Explicacion', type: 'textarea', dependsOn: 'accidentsWithSequelae', showWhen: 'Si' },
      { key: 'activeHealthAffiliation', label: 'Afiliacion activa al sistema de salud', type: 'radio', options: YES_NO },
      { key: 'eps', label: 'EPS', type: 'text' },
      { key: 'medicalEmergencyContactName', label: 'Contacto de emergencia medica - Nombre', type: 'text' },
      { key: 'medicalEmergencyContactRelationship', label: 'Parentesco', type: 'text' },
      { key: 'medicalEmergencyContactPhone', label: 'Telefono', type: 'text' }
    ]
  },
  {
    key: 'riskFactors',
    title: 'Habitos y factores de riesgo',
    fields: [
      { key: 'tobaccoUse', label: 'Consumo de cigarrillo/tabaco', type: 'radio', options: YES_NO_OCCASIONAL },
      { key: 'alcoholUse', label: 'Consumo de bebidas alcoholicas', type: 'radio', options: YES_NO_OCCASIONAL },
      { key: 'psychoactiveSubstancesUse', label: 'Consumo previo de sustancias psicoactivas', type: 'radio', options: YES_NO },
      { key: 'substanceUseTime', label: 'Momento aproximado del consumo', type: 'radio', options: ['Hace mas de un ano', 'En el ultimo ano', 'En los ultimos seis meses'], dependsOn: 'psychoactiveSubstancesUse', showWhen: 'Si' },
      { key: 'consumptionAffectedPerformance', label: 'Considera que algun consumo ha afectado su desempeno academico, laboral o familiar', type: 'radio', options: YES_NO },
      { key: 'preventionProgramParticipation', label: 'Participacion en programas de prevencion/orientacion/tratamiento', type: 'radio', options: YES_NO },
      { key: 'currentProfessionalSupport', label: 'Acompanamiento profesional actual', type: 'radio', options: YES_NO },
      { key: 'preventionGuidanceInterest', label: 'Interes en recibir orientacion sobre prevencion', type: 'radio', options: YES_NO },
      { key: 'healthyHabitsActivitiesWillingness', label: 'Disposicion para participar en actividades de promocion de habitos saludables', type: 'radio', options: YES_NO },
      { key: 'personalFamilySituationAffectsProcess', label: 'Situacion personal o familiar que pueda afectar su proceso academico', type: 'radio', options: YES_NO },
      { key: 'personalFamilySituationDescription', label: 'Descripcion de dicha situacion', type: 'textarea', dependsOn: 'personalFamilySituationAffectsProcess', showWhen: 'Si' }
    ]
  },
  {
    key: 'academicPerformance',
    title: 'Desempeno academico del aspirante',
    fields: [
      { key: 'bestSubject', label: 'Materia donde obtuvo mejores resultados', type: 'text' },
      { key: 'strengthAreas', label: 'Areas de mayor fortaleza', type: 'checkboxes', options: ['Lengua Castellana', 'Matematicas', 'Ciencias Naturales', 'Ciencias Sociales', 'Informatica', 'Ingles', 'Etica y Valores', 'Otra'] },
      { key: 'academicSkills', label: 'Habilidades academicas', type: 'checkboxes', options: ['Comprension de lectura', 'Redaccion de textos', 'Calculo matematico', 'Investigacion', 'Exposicion oral', 'Manejo de herramientas tecnologicas', 'Trabajo en equipo'] },
      { key: 'academicRecognitions', label: 'Reconocimientos academicos', type: 'radio', options: YES_NO },
      { key: 'academicRecognitionsDetails', label: 'Cuales', type: 'textarea', dependsOn: 'academicRecognitions', showWhen: 'Si' },
      { key: 'subjectsToStrengthen', label: 'Asignaturas que requieren fortalecimiento', type: 'textarea' },
      { key: 'studyHabits', label: 'Habitos de estudio', type: 'radio', options: ['Excelente', 'Bueno', 'Regular', 'Necesita mejorar'] },
      { key: 'weeklyStudyHours', label: 'Horas semanales dedicadas al estudio', type: 'radio', options: ['Menos de 5', 'Entre 5 y 10', 'Entre 11 y 15', 'Mas de 15'] },
      { key: 'taskResponsibility', label: 'Responsabilidad frente a tareas academicas', type: 'textarea' },
      { key: 'mainAcademicAchievement', label: 'Principal logro academico', type: 'textarea' },
      { key: 'areasToStrengthen', label: 'Areas que desea fortalecer', type: 'textarea' }
    ]
  },
  {
    key: 'programKnowledge',
    title: 'Conocimientos y motivacion frente al programa',
    fields: [
      { key: 'nursingUnderstanding', label: 'Que entiende por enfermeria', type: 'textarea' },
      { key: 'geriatricsUnderstanding', label: 'Que entiende por geriatria', type: 'textarea' },
      { key: 'gerontologyUnderstanding', label: 'Que entiende por gerontologia', type: 'textarea' },
      { key: 'geriatricsGerontologyDifference', label: 'Diferencia entre geriatria y gerontologia', type: 'textarea' },
      { key: 'studyReason', label: 'Razon por la que desea estudiar enfermeria o cuidado del adulto mayor', type: 'textarea' },
      { key: 'careExperience', label: 'Experiencia cuidando personas enfermas o adultos mayores', type: 'radio', options: YES_NO },
      { key: 'careExperienceDescription', label: 'Descripcion de dicha experiencia', type: 'textarea', dependsOn: 'careExperience', showWhen: 'Si' },
      { key: 'elderCareQualities', label: 'Cualidades necesarias para trabajar con adultos mayores', type: 'textarea' },
      { key: 'humanDignityMeaning', label: 'Significado del respeto por la dignidad humana', type: 'textarea' },
      { key: 'sadOlderAdultAction', label: 'Como actuaria frente a un adulto mayor triste o desmotivado', type: 'textarea' },
      { key: 'needsHelpNoStaffAction', label: 'Que haria si observa que un adulto mayor necesita ayuda y no hay personal disponible', type: 'textarea' },
      { key: 'patienceImportance', label: 'Importancia de la paciencia en la atencion de personas mayores', type: 'textarea' },
      { key: 'healthValues', label: 'Valores fundamentales para trabajar en salud', type: 'textarea' },
      { key: 'programExpectedLearning', label: 'Que espera aprender', type: 'textarea' },
      { key: 'communityContribution', label: 'Como puede contribuir al bienestar de adultos mayores de su comunidad', type: 'textarea' },
      { key: 'desiredWorkplace', label: 'Donde le gustaria trabajar al finalizar su formacion', type: 'textarea' },
      { key: 'humanizedServiceMeaning', label: 'Que significa brindar un servicio humanizado', type: 'textarea' },
      { key: 'practiceResponsibility', label: 'Disposicion para realizar practicas con responsabilidad, respeto y compromiso', type: 'radio', options: YES_NO },
      { key: 'mainTrainingChallenge', label: 'Mayor reto durante el proceso de formacion', type: 'textarea' },
      { key: 'refusesHelpReaction', label: 'Como reaccionaria si un paciente/adulto mayor se niega a recibir ayuda', type: 'textarea' },
      { key: 'scholarshipMeritReason', label: 'Por que considera que merece recibir la beca', type: 'textarea' }
    ]
  },
  {
    key: 'institutionalCommitment',
    title: 'Conocimiento de la Fundacion y compromiso institucional',
    fields: [
      { key: 'foundationSocialWorkKnowledge', label: 'Conocimiento de la labor social de la Fundacion', type: 'radio', options: YES_NO },
      { key: 'foundationSocialWorkDescription', label: 'Descripcion de lo que conoce', type: 'textarea', dependsOn: 'foundationSocialWorkKnowledge', showWhen: 'Si' },
      { key: 'callSource', label: 'Como se entero de la convocatoria', type: 'checkboxes', options: ['Redes sociales', 'Pagina web', 'Familiar o amigo', 'Institucion educativa', 'Otro'] },
      { key: 'foundationProgramReason', label: 'Razon por la que desea participar en programas apoyados por la Fundacion', type: 'textarea' },
      { key: 'understandsScholarshipResponsibility', label: 'Comprension de que la beca exige responsabilidad, disciplina y compromiso', type: 'radio', options: YES_NO },
      { key: 'studentRulesCommitment', label: 'Compromiso de cumplir reglamento estudiantil y normas', type: 'radio', options: YES_NO },
      { key: 'respectfulConductCommitment', label: 'Compromiso de mantener conducta respetuosa', type: 'radio', options: YES_NO },
      { key: 'punctualAttendanceCommitment', label: 'Compromiso de asistir puntualmente', type: 'radio', options: YES_NO },
      { key: 'socialCommunityParticipation', label: 'Participacion en actividades sociales y comunitarias', type: 'radio', options: YES_NO },
      { key: 'resourceCareCommitment', label: 'Compromiso de cuidar instalaciones, equipos y recursos', type: 'radio', options: YES_NO },
      { key: 'understandsNonComplianceConsequences', label: 'Comprension de las consecuencias del incumplimiento', type: 'radio', options: YES_NO },
      { key: 'trackingAuthorization', label: 'Autorizacion de seguimiento academico, disciplinario y de permanencia', type: 'radio', options: YES_NO }
    ]
  },
  {
    key: 'declaration',
    title: 'Declaracion de compromiso',
    fields: [
      { key: 'truthfulCompleteInformation', label: 'Confirmacion de que la informacion es veraz y completa', type: 'radio', options: YES_NO, required: true },
      { key: 'applicantName', label: 'Nombre del aspirante', type: 'text' },
      { key: 'identityDocument', label: 'Documento de identidad', type: 'text' },
      { key: 'signatureManagementSpace', label: 'Firma o espacio preparado para gestion de firma', type: 'text' },
      { key: 'signatureDate', label: 'Fecha', type: 'date' }
    ]
  }
];

@Component({
  selector: 'app-resume',
  standalone: true,
  imports: [ReactiveFormsModule, RouterLink, EmptyStateComponent, DatePipe],
  template: `
    <a class="back-link" routerLink="/hojas-de-vida">Volver a hojas de vida</a>

    @if (loading()) {
      <app-empty-state title="Cargando" message="Consultando hoja de vida." />
    } @else if (error()) {
      <app-empty-state title="Error" message="No fue posible cargar la hoja de vida." />
    } @else {
      <section class="resume-head">
        <div>
          <p class="section-kicker">Hoja de vida</p>
          <h2>{{ isNew() ? 'Nueva Hoja de Vida' : fullName() }}</h2>
          <p>{{ isNew() ? 'Registro completo del aspirante a beca' : documentLabel() }}</p>
        </div>
        @if (!isNew()) {
          <div class="page-actions">
            <button class="secondary-action" type="button" (click)="toggleViewMode()">{{ viewMode() ? 'Editar' : 'Solo lectura' }}</button>
          </div>
        }
      </section>

      @if (!isNew() && resume(); as data) {
        <section class="info-panel full-width">
          <div class="panel-title">
            <div class="user-summary">
              <span class="student-photo">
                @if (photoUrl(data.student.photoUrl); as url) {
                  <img [src]="url" [alt]="'Foto de ' + fullName()" />
                } @else {
                  <span>{{ studentInitials(data.student.firstName, data.student.lastName) }}</span>
                }
              </span>
              <div>
                <strong>{{ fullName() }}</strong>
                <span>{{ documentLabel() }}</span>
                <span>{{ data.student.email || 'Correo sin registrar' }}</span>
              </div>
            </div>
            <span class="status-pill" [class.inactive]="data.student.status === 'INACTIVE'">{{ data.student.status === 'ACTIVE' ? 'Activo' : 'Inactivo' }}</span>
          </div>
        </section>
      }

      <div class="section-progress" aria-label="Avance por secciones">
        @for (section of sections; track section.key) {
          <button type="button" [class.active]="activeSection() === section.key" [class.complete]="sectionComplete(section)" (click)="activeSection.set(section.key)">
            <span>{{ sectionComplete(section) ? 'OK' : '--' }}</span>
            {{ section.title }}
          </button>
        }
      </div>

      <form class="resume-form" [formGroup]="form" (ngSubmit)="save()" novalidate>
        @for (section of sections; track section.key) {
          <section class="info-panel full-width resume-section" [class.collapsed]="activeSection() !== section.key">
            <button class="section-toggle" type="button" (click)="activeSection.set(section.key)">
              <span>{{ section.title }}</span>
              <small>{{ sectionComplete(section) ? 'Completa' : 'Incompleta' }}</small>
            </button>
            @if (activeSection() === section.key) {
              <div class="field-grid" [formGroupName]="section.key">
                @for (field of section.fields; track field.key) {
                  @if (visible(section, field)) {
                    <label [class.textarea-field]="field.type === 'textarea'" [class.invalid-field]="fieldControl(section, field).invalid && fieldControl(section, field).touched">
                      <span>{{ field.label }}</span>
                      @switch (field.type) {
                        @case ('textarea') {
                          <textarea [formControlName]="field.key" [rows]="field.rows || 4"></textarea>
                        }
                        @case ('select') {
                          <select [formControlName]="field.key">
                            <option value="">Seleccione</option>
                            @for (option of field.options; track option) {
                              <option [value]="option">{{ option }}</option>
                            }
                          </select>
                        }
                        @case ('radio') {
                          <span class="choice-row">
                            @for (option of field.options; track option) {
                              <label class="choice"><input type="radio" [formControlName]="field.key" [value]="option" /> {{ option }}</label>
                            }
                          </span>
                        }
                        @case ('checkboxes') {
                          <span class="choice-row wrap">
                            @for (option of field.options; track option) {
                              <label class="choice"><input type="checkbox" [checked]="checked(section, field, option)" (change)="toggleOption(section, field, option, $event)" /> {{ option }}</label>
                            }
                          </span>
                        }
                        @case ('readonly') {
                          <input [value]="age()" readonly />
                        }
                        @default {
                          <input [type]="field.type" [formControlName]="field.key" />
                        }
                      }
                      @if (fieldControl(section, field).invalid && fieldControl(section, field).touched) {
                        <span class="field-error">Este campo es obligatorio.</span>
                      }
                    </label>
                  }
                }
              </div>
            }
          </section>
        }

        @if (saveError()) {
          <p class="form-error full-width">{{ saveError() }}</p>
        }
        @if (saveSuccess()) {
          <p class="form-success full-width">{{ saveSuccess() }}</p>
        }
        <div class="sticky-actions">
          <span class="meta-line">{{ viewMode() ? 'Modo solo lectura activo.' : 'Revisa los campos marcados antes de guardar.' }}</span>
          <div>
            <button class="secondary-action" type="button" routerLink="/hojas-de-vida">Cancelar</button>
            <button class="primary-action compact" type="submit" [disabled]="saving() || viewMode()">{{ saving() ? 'Guardando...' : 'Guardar cambios' }}</button>
          </div>
        </div>
      </form>

      @if (!isNew() && resume(); as data) {
        <section class="info-panel full-width enrollment-section">
          <div class="panel-title">
            <div>
              <p class="section-kicker">Matriculas</p>
              <h3>Gestion de matriculas</h3>
            </div>
            @if (editingEnrollment()) {
              <button class="ghost-action" type="button" (click)="resetEnrollment()">Cancelar edicion</button>
            }
          </div>
          <form class="enrollment-form" [formGroup]="enrollmentForm" (ngSubmit)="saveEnrollment(data.student.id)">
            <label>Periodo
              <input formControlName="periodCode" placeholder="2026-1" />
            </label>
            <label>Programa
              <input formControlName="program" placeholder="Programa de formacion" />
            </label>
            <label>Estado
              <select formControlName="status">
                <option value="ENROLLED">Matriculado</option>
                <option value="WITHDRAWN">Retirado</option>
                <option value="COMPLETED">Completado</option>
              </select>
            </label>
            <button class="primary-action compact" type="submit" [disabled]="enrollmentForm.invalid || savingEnrollment()">
              {{ savingEnrollment() ? 'Guardando...' : (editingEnrollment() ? 'Actualizar matricula' : 'Registrar matricula') }}
            </button>
            @if (enrollmentError()) {
              <p class="form-error full-width">{{ enrollmentError() }}</p>
            }
          </form>

          @if (data.enrollments.length === 0) {
            <app-empty-state title="Sin matriculas" message="No se han registrado matriculas para este estudiante." />
          } @else {
            <div class="document-list">
              @for (enrollment of data.enrollments; track enrollment.id) {
                <article class="document-row">
                  <div>
                    <strong>{{ enrollment.program }}</strong>
                    <span>Periodo {{ enrollment.periodCode }} - Actualizado {{ enrollment.updatedAt | date:'short' }}</span>
                    <span class="status-chip" [class.withdrawn]="enrollment.status === 'WITHDRAWN'" [class.completed]="enrollment.status === 'COMPLETED'">{{ enrollmentStatusLabel(enrollment.status) }}</span>
                  </div>
                  <div class="row-actions">
                    <button class="secondary-action" type="button" (click)="editEnrollment(enrollment)">Editar</button>
                  </div>
                </article>
              }
            </div>
          }
        </section>

        <section class="info-panel full-width document-section">
          <div>
            <p class="section-kicker">Documentos</p>
            <h3>Documentos adjuntos</h3>
          </div>
          <form class="document-form" [formGroup]="documentForm" (ngSubmit)="attach(data.student.id)">
            <label>Tipo
              <select formControlName="documentType">
                @for (type of documentTypes; track type.value) {
                  <option [value]="type.value">{{ type.label }}</option>
                }
              </select>
            </label>
            <label>Nombre visible <input formControlName="displayName" /></label>
            <label>Descripcion <input formControlName="description" /></label>
            <label>Archivo <input type="file" accept=".pdf,.jpg,.jpeg,.png,.webp,application/pdf,image/jpeg,image/png,image/webp" (change)="selectDocument($event)" /></label>
            <button class="secondary-action" type="button" (click)="resetDocument()">Cancelar</button>
            <button class="primary-action compact" type="submit" [disabled]="documentForm.invalid || !documentFile() || uploading()">
              {{ replacingDocument() ? 'Reemplazar documento' : 'Adjuntar documento' }}
            </button>
            @if (replacingDocument()) {
              <p class="form-error">Reemplazando: {{ replacingDocument()!.displayName }}</p>
            }
            @if (documentError()) {
              <p class="form-error">{{ documentError() }}</p>
            }
          </form>

          @if (data.documents.length === 0) {
            <app-empty-state title="Documentos" message="Este aspirante aun no tiene documentos adjuntos." />
          } @else {
            <div class="document-list">
              @for (document of data.documents; track document.id) {
                <article class="document-row">
                  <div>
                    <strong>{{ document.displayName }}</strong>
                    <span>{{ documentTypeLabel(document.documentType) }} - {{ document.createdAt | date:'short' }}</span>
                    <span class="document-meta">
                      <span>{{ sizeLabel(document.size) }}</span>
                      <span class="status-chip" [class.deleted]="document.status === 'DELETED'">{{ documentStatusLabel(document.status) }}</span>
                    </span>
                    <span>Usuario de carga: {{ document.uploadedByUserId }}</span>
                  </div>
                  <div class="row-actions">
                    <button class="secondary-action" type="button" (click)="download(data.student.id, document)">Descargar</button>
                    <button class="secondary-action" type="button" (click)="startReplace(document)">Reemplazar</button>
                    @if (canDelete()) {
                      <button class="danger-action" type="button" (click)="askDeleteDocument(document)">Eliminar</button>
                    }
                  </div>
                </article>
              }
            </div>
          }
        </section>

        <section class="info-panel full-width">
          <div>
            <p class="section-kicker">Trazabilidad</p>
            <h3>Historial de cambios</h3>
          </div>
          @if (data.history.length === 0) {
            <app-empty-state title="Historial" message="Aun no hay cambios registrados." />
          } @else {
            <div class="history-list">
              @for (event of data.history; track event.id) {
                <article>
                  <strong>{{ event.summary }}</strong>
                  <span>{{ event.actorUserId }} - {{ event.createdAt | date:'medium' }}</span>
                </article>
              }
            </div>
          }
        </section>

        @if (pendingDocumentDelete(); as document) {
          <div class="modal-backdrop" role="presentation">
            <section class="student-modal" role="dialog" aria-modal="true" aria-label="Confirmar eliminacion de documento">
              <header>
                <h3>Marcar documento como eliminado</h3>
                <button class="icon-only" type="button" aria-label="Cerrar" (click)="pendingDocumentDelete.set(null)">x</button>
              </header>
              <div class="modal-body">
                <p class="full-width">Desea marcar como eliminado el documento "{{ document.displayName }}"?</p>
              </div>
              <footer>
                <button class="secondary-action" type="button" (click)="pendingDocumentDelete.set(null)">Cancelar</button>
                <button class="danger-action" type="button" (click)="deleteDocument(data.student.id, document.id)">Marcar como eliminado</button>
              </footer>
            </section>
          </div>
        }
      }
    }
  `
})
export class ResumeComponent {
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly service = inject(StudentsService);
  private readonly auth = inject(AuthService);
  private readonly fb = inject(FormBuilder);
  private readonly notifications = inject(NotificationService);

  readonly sections = SECTIONS;
  readonly documentTypes = DOCUMENT_TYPES;
  readonly resume = signal<Resume | null>(null);
  readonly loading = signal(true);
  readonly error = signal(false);
  readonly saving = signal(false);
  readonly uploading = signal(false);
  readonly saveError = signal('');
  readonly saveSuccess = signal('');
  readonly documentError = signal('');
  readonly enrollmentError = signal('');
  readonly documentFile = signal<File | null>(null);
  readonly replacingDocument = signal<StudentDocument | null>(null);
  readonly editingEnrollment = signal<Enrollment | null>(null);
  readonly savingEnrollment = signal(false);
  readonly pendingDocumentDelete = signal<StudentDocument | null>(null);
  readonly activeSection = signal<Section['key']>('student');
  readonly viewMode = signal(false);
  readonly canDelete = computed(() => this.auth.user()?.role === 'ADMIN');
  readonly isNew = computed(() => !this.studentId);
  readonly age = computed(() => this.calculateAge((this.form.controls['student'] as FormGroup).controls['birthDate'].value));
  readonly fullName = computed(() => {
    const student = this.resume()?.student;
    return student ? `${student.firstName} ${student.lastName}` : '';
  });
  readonly documentLabel = computed(() => this.resume()?.student.documentNumber ? `Documento: ${this.resume()!.student.documentNumber}` : '');
  private readonly studentId = this.route.snapshot.paramMap.get('id') ?? '';

  readonly form = this.fb.group(this.buildForm());
  readonly documentForm = this.fb.nonNullable.group({
    documentType: [DOCUMENT_TYPES[0].value, Validators.required],
    displayName: ['', [Validators.required, Validators.maxLength(140)]],
    description: ['']
  });
  readonly enrollmentForm = this.fb.nonNullable.group({
    periodCode: ['', [Validators.required, Validators.maxLength(30)]],
    program: ['', [Validators.required, Validators.maxLength(140)]],
    status: ['ENROLLED' as 'ENROLLED' | 'WITHDRAWN' | 'COMPLETED', Validators.required]
  });

  constructor() {
    if (this.isNew()) {
      this.loading.set(false);
    } else {
      this.load();
    }
    ((this.form.controls['student'] as FormGroup).controls['birthDate'] as FormControl).valueChanges.subscribe(() => this.patchDeclaration());
    (this.form.controls['student'] as FormGroup).valueChanges.subscribe(() => this.patchDeclaration());
  }

  load(): void {
    this.loading.set(true);
    this.error.set(false);
    this.service.resume(this.studentId).pipe(finalize(() => this.loading.set(false))).subscribe({
      next: (resume) => {
        this.resume.set(resume);
        this.form.patchValue({
          student: {
            firstName: resume.student.firstName,
            lastName: resume.student.lastName,
            documentNumber: resume.student.documentNumber,
            birthDate: resume.student.birthDate,
            birthPlace: resume.student.birthPlace ?? '',
            address: resume.student.address ?? '',
            phone: resume.student.phone ?? '',
            email: resume.student.email ?? '',
            status: resume.student.status,
            ...(resume.details.personal ?? {})
          },
          ...resume.details
        });
        this.patchDeclaration();
      },
      error: () => this.error.set(true)
    });
  }

  save(): void {
    this.saveError.set('');
    this.saveSuccess.set('');
    this.trimGroup(this.form);
    if (this.form.invalid || this.saving()) {
      this.form.markAllAsTouched();
      this.saveError.set('Debe completar los campos obligatorios marcados.');
      return;
    }
    this.saving.set(true);
    const studentValue = this.studentPayload();
    const details = this.detailsPayload();
    const request = this.isNew()
      ? this.service.create(studentValue, null).pipe(
          switchMap((student) => this.service.updateResumeDetails(student.id, details).pipe(tap(() => void this.router.navigate(['/hojas-de-vida', student.id]))))
        )
      : this.service.update(this.studentId, studentValue, null).pipe(switchMap(() => this.service.updateResumeDetails(this.studentId, details)));

    request.pipe(finalize(() => this.saving.set(false))).subscribe({
      next: () => {
        this.saveSuccess.set('Hoja de vida guardada correctamente.');
        this.notifications.show('Hoja de vida guardada correctamente.');
        if (!this.isNew()) {
          this.load();
        }
      },
      error: (error: ApiError) => this.saveError.set(error.message || 'No fue posible guardar la hoja de vida.')
    });
  }

  toggleViewMode(): void {
    this.viewMode.update((value) => !value);
    if (this.viewMode()) {
      this.form.disable({ emitEvent: false });
    } else {
      this.form.enable({ emitEvent: false });
    }
  }

  visible(section: Section, field: Field): boolean {
    if (!field.dependsOn) {
      return true;
    }
    const value = this.sectionGroup(section).get(field.dependsOn)?.value;
    const expected = Array.isArray(field.showWhen) ? field.showWhen : [field.showWhen];
    return expected.includes(value);
  }

  fieldControl(section: Section, field: Field): AbstractControl {
    return this.sectionGroup(section).get(field.key) ?? new FormControl('');
  }

  checked(section: Section, field: Field, option: string): boolean {
    const value = this.fieldControl(section, field).value;
    return Array.isArray(value) && value.includes(option);
  }

  toggleOption(section: Section, field: Field, option: string, event: Event): void {
    const input = event.target as HTMLInputElement;
    const control = this.fieldControl(section, field);
    const current = Array.isArray(control.value) ? [...control.value] : [];
    control.setValue(input.checked ? [...current, option] : current.filter((item) => item !== option));
    control.markAsDirty();
  }

  sectionComplete(section: Section): boolean {
    return section.fields.filter((field) => this.visible(section, field)).every((field) => {
      if (field.type === 'readonly') {
        return true;
      }
      if (!field.required) {
        return true;
      }
      const value = this.fieldControl(section, field).value;
      return Array.isArray(value) ? value.length > 0 : String(value ?? '').trim().length > 0;
    });
  }

  attach(studentId: string): void {
    const file = this.documentFile();
    if (!file || this.documentForm.invalid || this.uploading()) {
      this.documentForm.markAllAsTouched();
      this.documentError.set('Debe seleccionar un archivo y completar los datos obligatorios.');
      return;
    }
    const value = this.documentForm.getRawValue();
    this.uploading.set(true);
    const replacing = this.replacingDocument();
    const request = replacing
      ? this.service.replaceDocument(studentId, replacing.id, value.documentType, value.displayName, value.description, file)
      : this.service.attachDocument(studentId, value.documentType, value.displayName, value.description, file);
    request.pipe(finalize(() => this.uploading.set(false))).subscribe({
      next: () => {
        this.documentError.set('');
        this.notifications.show(replacing ? 'Documento reemplazado correctamente.' : 'Documento adjuntado correctamente.');
        this.resetDocument();
        this.load();
      },
      error: (error: ApiError) => this.documentError.set(error.message || 'No fue posible guardar el documento.')
    });
  }

  saveEnrollment(studentId: string): void {
    if (this.enrollmentForm.invalid || this.savingEnrollment()) {
      this.enrollmentForm.markAllAsTouched();
      this.enrollmentError.set('Debe completar periodo, programa y estado.');
      return;
    }
    this.savingEnrollment.set(true);
    this.enrollmentError.set('');
    const value = this.enrollmentForm.getRawValue() as EnrollmentFormValue;
    const editing = this.editingEnrollment();
    const request = editing
      ? this.service.updateEnrollment(studentId, editing.id, value)
      : this.service.createEnrollment(studentId, value);

    request.pipe(finalize(() => this.savingEnrollment.set(false))).subscribe({
      next: () => {
        this.notifications.show(editing ? 'Matricula actualizada correctamente.' : 'Matricula registrada correctamente.');
        this.resetEnrollment();
        this.load();
      },
      error: (error: ApiError) => this.enrollmentError.set(error.message || 'No fue posible guardar la matricula.')
    });
  }

  editEnrollment(enrollment: Enrollment): void {
    this.editingEnrollment.set(enrollment);
    this.enrollmentError.set('');
    this.enrollmentForm.reset({
      periodCode: enrollment.periodCode,
      program: enrollment.program,
      status: enrollment.status
    });
  }

  resetEnrollment(): void {
    this.editingEnrollment.set(null);
    this.enrollmentError.set('');
    this.enrollmentForm.reset({ periodCode: '', program: '', status: 'ENROLLED' });
  }

  download(studentId: string, document: StudentDocument): void {
    this.service.downloadDocument(studentId, document.id).subscribe((response) => {
      const blob = response.body;
      if (!blob) {
        return;
      }
      const url = URL.createObjectURL(blob);
      const link = window.document.createElement('a');
      link.href = url;
      link.download = document.originalName;
      link.click();
      URL.revokeObjectURL(url);
    });
  }

  askDeleteDocument(document: StudentDocument): void {
    this.pendingDocumentDelete.set(document);
  }

  deleteDocument(studentId: string, documentId: string): void {
    this.service.deleteDocument(studentId, documentId).subscribe({
      next: () => {
        this.pendingDocumentDelete.set(null);
        this.notifications.show('Documento marcado como eliminado.');
        this.load();
      },
      error: () => this.error.set(true)
    });
  }

  selectDocument(event: Event): void {
    const input = event.target as HTMLInputElement;
    this.documentFile.set(input.files?.item(0) ?? null);
  }

  resetDocument(): void {
    this.documentFile.set(null);
    this.replacingDocument.set(null);
    this.documentError.set('');
    this.documentForm.reset({ documentType: DOCUMENT_TYPES[0].value, displayName: '', description: '' });
  }

  startReplace(document: StudentDocument): void {
    this.replacingDocument.set(document);
    this.documentFile.set(null);
    this.documentForm.patchValue({
      documentType: document.documentType,
      displayName: document.displayName,
      description: document.description ?? ''
    });
  }

  sizeLabel(size: number): string {
    return `${(size / 1024).toFixed(1)} KB`;
  }

  documentTypeLabel(value: string): string {
    return DOCUMENT_TYPES.find((type) => type.value === value)?.label ?? value;
  }

  documentStatusLabel(value: string): string {
    const labels: Record<string, string> = {
      ACTIVE: 'Activo',
      REPLACED: 'Reemplazado',
      DELETED: 'Eliminado'
    };
    return labels[value] ?? value;
  }

  enrollmentStatusLabel(value: string): string {
    const labels: Record<string, string> = {
      ENROLLED: 'Matriculado',
      WITHDRAWN: 'Retirado',
      COMPLETED: 'Completado'
    };
    return labels[value] ?? value;
  }

  photoUrl(path: string | null): string | null {
    return this.service.photoUrl(path);
  }

  studentInitials(firstName: string, lastName: string): string {
    return `${firstName.at(0) ?? ''}${lastName.at(0) ?? ''}`.toUpperCase();
  }

  private buildForm(): Record<string, FormGroup> {
    const groups: Record<string, FormGroup> = {};
    for (const section of SECTIONS) {
      const controls: Record<string, FormControl> = {};
      for (const field of section.fields) {
        if (field.type === 'readonly') {
          continue;
        }
        const validators = section.key === 'student' && field.required ? [Validators.required] : [];
        controls[field.key] = new FormControl(field.type === 'checkboxes' ? [] : '', { nonNullable: true, validators });
      }
      groups[section.key] = this.fb.group(controls);
    }
    return groups;
  }

  private sectionGroup(section: Section): FormGroup {
    return this.form.get(String(section.key)) as FormGroup;
  }

  private studentPayload(): StudentFormValue {
    const value = (this.form.controls['student'] as FormGroup).getRawValue() as Record<string, string>;
    return {
      firstName: value['firstName'],
      lastName: value['lastName'],
      documentNumber: value['documentNumber'],
      birthDate: value['birthDate'],
      birthPlace: value['birthPlace'],
      address: value['address'],
      phone: value['phone'],
      email: value['email'],
      status: (value['status'] || 'ACTIVE') as StudentStatus
    };
  }

  private detailsPayload(): ResumeDetails {
    const raw = this.form.getRawValue() as unknown as Record<string, Record<string, unknown>>;
    return {
      personal: this.mergePersonal(raw['student']),
      socioeconomic: raw['socioeconomic'] ?? {},
      academic: raw['academic'] ?? {},
      motivation: raw['motivation'] ?? {},
      availability: raw['availability'] ?? {},
      foundationKnowledge: raw['foundationKnowledge'] ?? {},
      authorizations: raw['authorizations'] ?? {},
      health: raw['health'] ?? {},
      riskFactors: raw['riskFactors'] ?? {},
      academicPerformance: raw['academicPerformance'] ?? {},
      programKnowledge: raw['programKnowledge'] ?? {},
      institutionalCommitment: raw['institutionalCommitment'] ?? {},
      declaration: raw['declaration'] ?? {}
    };
  }

  private mergePersonal(student: Record<string, unknown>): Record<string, unknown> {
    return {
      documentType: student['documentType'],
      municipality: student['municipality'],
      department: student['department'],
      civilStatus: student['civilStatus'],
      hasChildren: student['hasChildren'],
      childrenCount: student['childrenCount'],
      emergencyContactName: student['emergencyContactName'],
      emergencyContactPhone: student['emergencyContactPhone']
    };
  }

  private patchDeclaration(): void {
    const student = (this.form.controls['student'] as FormGroup).getRawValue() as Record<string, string>;
    (this.form.controls['declaration'] as FormGroup).patchValue({
      applicantName: `${student['firstName'] ?? ''} ${student['lastName'] ?? ''}`.trim(),
      identityDocument: student['documentNumber'] ?? ''
    }, { emitEvent: false });
  }

  private calculateAge(value: string): string {
    if (!value) {
      return '';
    }
    const birth = new Date(`${value}T00:00:00`);
    const today = new Date();
    let age = today.getFullYear() - birth.getFullYear();
    const m = today.getMonth() - birth.getMonth();
    if (m < 0 || (m === 0 && today.getDate() < birth.getDate())) {
      age--;
    }
    return Number.isFinite(age) && age >= 0 ? String(age) : '';
  }

  private trimGroup(group: FormGroup): void {
    Object.values(group.controls).forEach((control) => {
      if (control instanceof FormGroup) {
        this.trimGroup(control);
        return;
      }
      if (typeof control.value === 'string') {
        control.setValue(control.value.trim(), { emitEvent: false });
      }
    });
  }
}
