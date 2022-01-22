import {BrowserModule} from '@angular/platform-browser';
import {NgModule} from '@angular/core';
import {NgbModule} from '@ng-bootstrap/ng-bootstrap';
import {HttpClientModule} from '@angular/common/http';
import {FontAwesomeModule} from '@fortawesome/angular-fontawesome';
import {FormsModule} from '@angular/forms';

import {AppComponent} from './app.component';
import {JogComponent} from './components/jog/jog.component';
import {DroComponent} from './components/dro/dro.component';
import {MachineControlComponent} from './components/machine-control/machine-control.component';
import {SendFileComponent} from './components/send-file/send-file.component';
import {SendCommandsComponent} from './components/send-commands/send-commands.component';
import {TabComponent} from './components/tab/tab.component';
import {TabSetComponent} from './components/tab-set/tab-set.component';
import {ConnectComponent} from './components/connect/connect.component';
import {MacrosComponent} from './components/macros/macros.component';

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
}
