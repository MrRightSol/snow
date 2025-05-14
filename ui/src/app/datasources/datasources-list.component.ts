import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatTableModule } from '@angular/material/table';
import { MatButtonModule } from '@angular/material/button';
import { RouterLink } from '@angular/router';
import { DataSourceService } from './datasources.service';
import { Observable } from 'rxjs';

/**
 * Component to display a list of DataSource configs.
 */
@Component({
  standalone: true,
  selector: 'app-datasources-list',
  imports: [CommonModule, MatTableModule, MatButtonModule, RouterLink],
  templateUrl: './datasources-list.component.html',
  styleUrls: ['./datasources-list.component.scss']
})
export class DatasourcesListComponent implements OnInit {
  displayedColumns = ['id', 'name', 'url', 'instanceName', 'actions'];
  // Observable for DataSource configurations; '!' asserts it will be initialized before use
  dataSourceConfigs$!: Observable<any[]>;

  constructor(private dsService: DataSourceService) {}

  ngOnInit(): void {
    this.dataSourceConfigs$ = this.dsService.list();
  }

  importMetadata(id: number): void {
    this.dsService.import(id).subscribe(() => {
      // refresh list after import
      this.dataSourceConfigs$ = this.dsService.list();
    });
  }
}