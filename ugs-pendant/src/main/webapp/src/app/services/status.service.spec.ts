import { TestBed } from '@angular/core/testing';

import { StatusService } from './status.service';

describe('StatusService', () => {
  beforeEach(() => TestBed.configureTestingModule({}));

  it('should be created', () => {
    const service: StatusService = TestBed.get(StatusService);
    expect(service).toBeTruthy();
  });
});
