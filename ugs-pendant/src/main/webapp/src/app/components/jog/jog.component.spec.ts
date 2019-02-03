import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { JogComponent } from './jog.component';

describe('JogComponent', () => {
  let component: JogComponent;
  let fixture: ComponentFixture<JogComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ JogComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(JogComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
