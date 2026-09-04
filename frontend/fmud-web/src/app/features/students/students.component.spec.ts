import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting, HttpTestingController } from '@angular/common/http/testing';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { StudentsComponent } from './students.component';

describe('StudentsComponent', () => {
  let fixture: ComponentFixture<StudentsComponent>;
  let http: HttpTestingController;

  beforeEach(() => {
    localStorage.setItem('fmud.user', JSON.stringify({ id: '1', name: 'Admin', email: 'a@fmud.local', role: 'ADMIN' }));
    localStorage.setItem('fmud.accessToken', 'token');
    TestBed.configureTestingModule({
      imports: [StudentsComponent],
      providers: [provideRouter([]), provideHttpClient(), provideHttpClientTesting()]
    });
    fixture = TestBed.createComponent(StudentsComponent);
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    http.verify();
    localStorage.clear();
  });

  it('lists student cards and searches', () => {
    fixture.detectChanges();
    http.expectOne('/api/students?page=0&size=12').flush({
      content: [student()],
      page: 0,
      size: 12,
      totalElements: 1,
      totalPages: 1
    });
    fixture.detectChanges();

    expect(fixture.nativeElement.textContent).toContain('12345678');

    fixture.componentInstance.filters.controls.search.setValue('Ana');
    fixture.componentInstance.search();
    http.expectOne('/api/students?page=0&size=12&search=Ana').flush({ content: [], page: 0, size: 12, totalElements: 0, totalPages: 0 });
  });

  function student() {
    return {
      id: 's1',
      firstName: 'Ana',
      lastName: 'Perez',
      documentNumber: '12345678',
      birthDate: '2012-01-01',
      birthPlace: null,
      address: null,
      phone: null,
      email: null,
      photoUrl: null,
      status: 'ACTIVE',
      createdAt: '2026-01-01T00:00:00Z',
      updatedAt: '2026-01-01T00:00:00Z'
    };
  }
});
