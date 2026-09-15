import { fakeAsync, TestBed, tick } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { BackendReadinessService } from './backend-readiness.service';
import { environment } from '../../../environments/environment';

describe('BackendReadinessService', () => {
  let service: BackendReadinessService;
  let http: HttpTestingController;
  const url = `${environment.apiBaseUrl}/health/ready`;

  beforeEach(() => {
    TestBed.configureTestingModule({ providers: [provideHttpClient(), provideHttpClientTesting()] });
    service = TestBed.inject(BackendReadinessService);
    http = TestBed.inject(HttpTestingController);
  });
  afterEach(() => http.verify());

  it('recovers from Render startup errors before reporting readiness', fakeAsync(() => {
    let ready = false;
    service.waitUntilReady().subscribe(() => ready = true);
    for (const status of [502, 429, 503]) {
      http.expectOne(url).flush('Starting', { status, statusText: 'Starting' });
      expect(ready).toBeFalse();
      tick(3000);
    }
    http.expectOne(url).flush({ status: 'UP' });
    expect(ready).toBeTrue();
  }));

  it('does not accept an HTML startup page as readiness', fakeAsync(() => {
    let ready = false;
    service.waitUntilReady().subscribe(() => ready = true);
    http.expectOne(url).flush('<html>Starting</html>');
    expect(ready).toBeFalse();
    tick(3000);
    http.expectOne(url).flush({ status: 'UP' });
    expect(ready).toBeTrue();
  }));

  it('ends a stalled startup within four minutes', fakeAsync(() => {
    let message = '';
    service.waitUntilReady().subscribe({ error: error => message = error.message });
    for (let elapsed = 0; elapsed < 234000; elapsed += 18000) {
      const request = http.expectOne(url);
      tick(15000);
      expect(request.cancelled).toBeTrue();
      tick(3000);
    }
    const last = http.expectOne(url);
    tick(6000);
    expect(last.cancelled).toBeTrue();
    expect(message).toContain('a tiempo');
  }));
});
