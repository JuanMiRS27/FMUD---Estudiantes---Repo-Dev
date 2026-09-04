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
    http.expectOne('/api/students/s1/resume').flush({
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
        updatedAt: '2026-01-01T00:00:00Z'
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
        declaration: {}
      },
      enrollments: [],
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
    });
    fixture.detectChanges();

    expect(fixture.nativeElement.textContent).toContain('Cedula');
    expect(fixture.nativeElement.textContent).not.toContain('Eliminar');
  });
});
