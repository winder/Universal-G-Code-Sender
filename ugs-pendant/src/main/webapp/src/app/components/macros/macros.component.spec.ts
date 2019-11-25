import { async, ComponentFixture, TestBed } from '@angular/core/testing';
import { HttpClientTestingModule } from '@angular/common/http/testing';

import { MacrosComponent } from './macros.component';

describe('MacrosComponent', () => {
  let component: MacrosComponent;
  let fixture: ComponentFixture<MacrosComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ MacrosComponent ],
      imports: [ HttpClientTestingModule ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(MacrosComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
