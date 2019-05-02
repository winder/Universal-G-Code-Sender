import { TestBed } from '@angular/core/testing';

import { MacrosService } from './macros.service';

describe('MacrosService', () => {
  beforeEach(() => TestBed.configureTestingModule({}));

  it('should be created', () => {
    const service: MacrosService = TestBed.get(MacrosService);
    expect(service).toBeTruthy();
  });
});
