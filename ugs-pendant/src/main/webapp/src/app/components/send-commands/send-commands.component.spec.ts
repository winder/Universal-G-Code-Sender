import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { SendCommandsComponent } from './send-commands.component';

describe('SendCommandsComponent', () => {
  let component: SendCommandsComponent;
  let fixture: ComponentFixture<SendCommandsComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ SendCommandsComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(SendCommandsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
