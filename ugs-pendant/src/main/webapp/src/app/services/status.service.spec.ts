import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule } from '@angular/common/http/testing';

import { StatusService } from './status.service';

describe('StatusService', () => {
  beforeEach(() => TestBed.configureTestingModule({
    imports: [HttpClientTestingModule]
  }));

  it('should be created', () => {
    const service: StatusService = TestBed.get(StatusService);
    expect(service).toBeTruthy();
  });
});
