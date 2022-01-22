import {Component, OnInit} from '@angular/core';
import {MacrosService} from '../../services/macros.service'
import {Macro} from '../../model/macro';

@Component({
    selector: 'app-macros',
    templateUrl: './macros.component.html',
    styleUrls: ['./macros.component.scss']
})
export class MacrosComponent implements OnInit {

    private _macroList: Macro[];

    constructor(private macrosService: MacrosService) {
    }

    ngOnInit() {
        this.macrosService.getMacroList().subscribe(macros => this._macroList = macros);
    }

    public runMacro(macro) {
        this.macrosService.runMacro(macro).subscribe();
    }

    get macroList(): Macro[] {
        return this._macroList;
    }
}
