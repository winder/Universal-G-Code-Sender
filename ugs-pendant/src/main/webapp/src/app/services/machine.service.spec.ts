import { TestBed } from '@angular/core/testing';

import { MachineService } from './machine.service';

describe('MachineService', () => {
  beforeEach(() => TestBed.configureTestingModule({}));

  it('should be created', () => {
    const service: MachineService = TestBed.get(MachineService);
    expect(service).toBeTruthy();
  });
});
