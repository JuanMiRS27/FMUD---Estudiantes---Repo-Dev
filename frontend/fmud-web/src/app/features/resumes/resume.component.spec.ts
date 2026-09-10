import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ActivatedRoute } from '@angular/router';
import { provideRouter } from '@angular/router';
import { ResumeComponent } from './resume.component';

describe('ResumeComponent', () => {
  let fixture: ComponentFixture<ResumeComponent>;
  let http: HttpTestingController;

  beforeEach(() => {
    localStorage.setItem('fmud.user', JSON.stringify({ id: '1', name: 'Secretaria', email: 's@fmud.local', role: 'SECRETARIO' }));
    localStorage.setItem('fmud.accessToken', 'token');
    TestBed.configureTestingModule({
      imports: [ResumeComponent],
      providers: [
        provideRouter([]),
        provideHttpClient(),
        provideHttpClientTesting(),
        { provide: ActivatedRoute, useValue: { snapshot: { paramMap: new Map([['id', 's1']]) } } }
      ]
    });
    fixture = TestBed.createComponent(ResumeComponent);
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    http.verify();
    localStorage.clear();
  });

  it('shows resume documents without delete action for secretary', () => {
    fixture.detectChanges();
    http.expectOne('/api/students/s1/resume').flush(resumeFixture({
      student: {
        address: 'Calle 1',
        phone: '+57300'
      },
      documents: [{
        id: 'd1',
        studentId: 's1',
        documentType: 'Documento de identidad',
        displayName: 'Cedula',
        originalName: 'cedula.pdf',
        contentType: 'application/pdf',
        size: 1024,
        description: null,
        status: 'ACTIVE',
        uploadedByUserId: 'user-1',
        createdAt: '2026-01-01T00:00:00Z',
        updatedAt: '2026-01-01T00:00:00Z'
      }],
      history: []
    }));
    fixture.detectChanges();

    expect(fixture.nativeElement.textContent).toContain('Cedula');
    expect(fixture.nativeElement.textContent).not.toContain('Eliminar');
  });

  it('reloads saved resume before showing success and patches the form with backend values', () => {
    fixture.detectChanges();
    http.expectOne('/api/students/s1/resume').flush(resumeFixture({
      student: {
        address: 'Calle inicial'
      },
      details: {
        personal: {
          hasChildren: false
        },
        socioeconomic: {
          currentlyWorks: false
        },
        foundationKnowledge: {
          callSource: ['Pagina web']
        }
      }
    }));

    const component = fixture.componentInstance;
    component.form.patchValue({
      student: {
        firstName: 'Ana',
        lastName: 'Perez',
        documentNumber: '12345678',
        birthDate: '2012-01-01',
        status: 'ACTIVE',
        address: 'Calle editada',
        hasChildren: 'Si',
        childrenCount: 2
      },
      socioeconomic: {
        currentlyWorks: 'No'
      },
      foundationKnowledge: {
        callSource: ['Redes sociales', 'Pagina web']
      }
    });

    expect(component.form.valid).toBeTrue();
    component.save();

    const updateStudent = http.expectOne('/api/students/s1');
    expect(updateStudent.request.method).toBe('PUT');
    updateStudent.flush(resumeFixture().student);

    http.expectNone('/api/students/s1/resume');

    const updateDetails = http.expectOne('/api/students/s1/resume/details');
    expect(updateDetails.request.method).toBe('PUT');
    expect(updateDetails.request.body.personal.hasChildren).toBeTrue();
    expect(updateDetails.request.body.socioeconomic.currentlyWorks).toBeFalse();
    expect(updateDetails.request.body.foundationKnowledge.callSource).toEqual(['Redes sociales', 'Pagina web']);
    updateDetails.flush({});

    const reload = http.expectOne('/api/students/s1/resume');
    expect(reload.request.method).toBe('GET');
    reload.flush(resumeFixture({
      student: {
        address: 'Calle normalizada por backend'
      },
      details: {
        personal: {
          hasChildren: true,
          childrenCount: 2
        },
        socioeconomic: {
          currentlyWorks: false
        },
        foundationKnowledge: {
          callSource: ['Pagina web', 'Redes sociales']
        }
      }
    }));

    const studentGroup = component.form.controls['student'];
    expect(studentGroup.get('address')?.value).toBe('Calle normalizada por backend');
    expect(studentGroup.get('hasChildren')?.value).toBe('Si');
    expect(component.form.get('socioeconomic.currentlyWorks')?.value).toBe('No');
    expect(component.form.get('foundationKnowledge.callSource')?.value).toEqual(['Pagina web', 'Redes sociales']);
    expect(component.form.pristine).toBeTrue();
    expect(component.saveSuccess()).toBe('Hoja de vida guardada correctamente.');
  });
});

function resumeFixture(overrides: Record<string, any> = {}) {
  return {
    student: {
      id: 's1',
      firstName: 'Ana',
      lastName: 'Perez',
      documentNumber: '12345678',
      birthDate: '2012-01-01',
      birthPlace: 'Bogota',
      address: 'Calle 1',
      phone: '+57300',
      email: 'ana@example.com',
      photoUrl: null,
      status: 'ACTIVE',
      createdAt: '2026-01-01T00:00:00Z',
      updatedAt: '2026-01-01T00:00:00Z',
      ...(overrides['student'] ?? {})
    },
    details: {
      personal: {},
      socioeconomic: {},
      academic: {},
      motivation: {},
      availability: {},
      foundationKnowledge: {},
      authorizations: {},
      health: {},
      riskFactors: {},
      academicPerformance: {},
      programKnowledge: {},
      institutionalCommitment: {},
      declaration: {},
      ...(overrides['details'] ?? {})
    },
    enrollments: overrides['enrollments'] ?? [],
    documents: overrides['documents'] ?? [],
    history: overrides['history'] ?? []
  };
}
