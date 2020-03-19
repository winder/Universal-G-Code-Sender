import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule } from '@angular/common/http/testing';

import { MachineService } from './machine.service';

describe('MachineService', () => {
  beforeEach(() => TestBed.configureTestingModule({
    imports: [HttpClientTestingModule]
  }));

  it('should be created', () => {
    const service: MachineService = TestBed.get(MachineService);
    expect(service).toBeTruthy();
  });
});
