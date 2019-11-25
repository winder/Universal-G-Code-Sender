import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule } from '@angular/common/http/testing';

import { MacrosService } from './macros.service';

describe('MacrosService', () => {
  beforeEach(() => TestBed.configureTestingModule({
    imports: [HttpClientTestingModule]
  }));

  it('should be created', () => {
    const service: MacrosService = TestBed.get(MacrosService);
    expect(service).toBeTruthy();
  });
});
