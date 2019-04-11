import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { DroComponent } from './dro.component';

describe('DroComponent', () => {
  let component: DroComponent;
  let fixture: ComponentFixture<DroComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ DroComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(DroComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
