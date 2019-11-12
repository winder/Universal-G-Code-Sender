import { async, ComponentFixture, TestBed } from '@angular/core/testing';
import { FaIconComponent } from "@fortawesome/angular-fontawesome";
import { HttpClientTestingModule } from '@angular/common/http/testing';

import { SendFileComponent } from './send-file.component';

describe('SendFileComponent', () => {
  let component: SendFileComponent;
  let fixture: ComponentFixture<SendFileComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ SendFileComponent, FaIconComponent ],
      imports: [HttpClientTestingModule]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(SendFileComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
