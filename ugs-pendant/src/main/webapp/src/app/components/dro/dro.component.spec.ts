import { async, ComponentFixture, TestBed } from '@angular/core/testing';
import { FaIconComponent } from "@fortawesome/angular-fontawesome";
import { HttpClientTestingModule } from '@angular/common/http/testing';

import { DroComponent } from './dro.component';

describe('DroComponent', () => {
  let component: DroComponent;
  let fixture: ComponentFixture<DroComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ DroComponent, FaIconComponent ],
      imports: [ HttpClientTestingModule ]
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
