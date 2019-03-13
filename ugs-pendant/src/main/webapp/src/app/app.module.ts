import { BrowserModule } from '@angular/platform-browser';
import { NgModule } from '@angular/core';
import { NgbModule } from '@ng-bootstrap/ng-bootstrap';
import { HttpClientModule } from '@angular/common/http';
import { FontAwesomeModule } from '@fortawesome/angular-fontawesome';
import { library } from '@fortawesome/fontawesome-svg-core';
import { faStop, faPlay, faPause, faFolderOpen, faChevronCircleUp, faChevronCircleDown,
  faChevronCircleLeft, faChevronCircleRight, faArrowCircleUp, faArrowCircleDown, faBackspace } from '@fortawesome/free-solid-svg-icons';
import { FormsModule } from '@angular/forms';

import { AppComponent } from './app.component';
import { JogComponent } from './components/jog/jog.component';
import { DroComponent } from './components/dro/dro.component';
import { MachineControlComponent } from './components/machine-control/machine-control.component';
import { SendFileComponent } from './components/send-file/send-file.component';
import { SendCommandsComponent } from './components/send-commands/send-commands.component';
import { TabComponent } from './components/tab/tab.component';
import { TabSetComponent } from './components/tab-set/tab-set.component';
import { ConnectComponent } from './components/connect/connect.component';
import { MacrosComponent } from './components/macros/macros.component';

@NgModule({
  declarations: [
    AppComponent,
    JogComponent,
    DroComponent,
    MachineControlComponent,
    SendFileComponent,
    SendCommandsComponent,
    TabComponent,
    TabSetComponent,
    ConnectComponent,
    MacrosComponent
  ],
  imports: [
    BrowserModule,
    NgbModule,
    HttpClientModule,
    FontAwesomeModule,
    FormsModule
  ],
  providers: [],
  bootstrap: [AppComponent]
})
export class AppModule {
  constructor() {
      // Add an icon to the library for convenient access in other components
      library.add(faStop);
      library.add(faPlay);
      library.add(faPause);
      library.add(faFolderOpen);
      library.add(faChevronCircleUp);
      library.add(faChevronCircleDown);
      library.add(faChevronCircleLeft);
      library.add(faChevronCircleRight);
      library.add(faArrowCircleUp);
      library.add(faArrowCircleDown);
      library.add(faBackspace);
  }
}
