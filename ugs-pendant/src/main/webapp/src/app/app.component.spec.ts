import { TestBed, async } from '@angular/core/testing';
import { AppComponent } from './app.component';
import { FormsModule } from '@angular/forms';
import { FaIconComponent } from "@fortawesome/angular-fontawesome";
import { HttpClientTestingModule } from '@angular/common/http/testing';

import { TabComponent } from './components/tab/tab.component';
import { TabSetComponent } from './components/tab-set/tab-set.component';
import { ConnectComponent } from './components/connect/connect.component';
import { SendCommandsComponent } from './components/send-commands/send-commands.component';
import { SendFileComponent } from './components/send-file/send-file.component';
import { MacrosComponent } from './components/macros/macros.component';
import { JogComponent } from './components/jog/jog.component';
import { MachineControlComponent } from './components/machine-control/machine-control.component';
import { DroComponent } from './components/dro/dro.component';


describe('AppComponent', () => {
  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [
        FaIconComponent,
        AppComponent,
        TabComponent,
        TabSetComponent,
        ConnectComponent,
        SendCommandsComponent,
        SendFileComponent,
        MacrosComponent,
        JogComponent,
        MachineControlComponent,
        DroComponent
      ],
      imports: [
        FormsModule,
        HttpClientTestingModule
      ]
    }).compileComponents();
  }));

  it('should create the app', () => {
    const fixture = TestBed.createComponent(AppComponent);
    const app = fixture.debugElement.componentInstance;
    expect(app).toBeTruthy();
  });
});
