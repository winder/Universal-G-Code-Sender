import { Component, OnInit } from '@angular/core';
import { MacrosService } from '../../services/macros.service'
import { Macro } from '../../model/macro';

@Component({
  selector: 'app-macros',
  templateUrl: './macros.component.html',
  styleUrls: ['./macros.component.scss']
})
export class MacrosComponent implements OnInit {

  private macroList:Macro[];

  constructor(private macrosService:MacrosService) { }

  ngOnInit() {
    this.macrosService.getMacroList().subscribe(macros => this.macroList = macros);
  }

  public runMacro(macro) {
    this.macrosService.runMacro(macro).subscribe();
  }
}
