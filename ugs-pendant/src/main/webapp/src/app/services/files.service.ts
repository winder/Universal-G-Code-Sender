import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs/Observable';

@Injectable({
  providedIn: 'root'
})
export class FilesService {

  constructor(private http:HttpClient) { }

  send(): Observable<any> {
    return this.http.post('/api/files/send', null);
  }

  pause(): Observable<any> {
    return this.http.get('/api/files/pause');
  }

  cancel(): Observable<any> {
    return this.http.get('/api/files/cancel');
  }

  uploadAndOpen(file:File): Observable<any> {
    let formData:FormData = new FormData();
    formData.append('file', file, file.name);
    return this.http.post("/api/files/uploadAndOpen", formData)
      .map((response: Response) => {
          return response;
      });
  }
}
