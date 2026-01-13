import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, map } from 'rxjs';

export type Provincia = {
  id: string;
  nombre: string;
};

export type Departamento = {
  id: string;
  nombre: string;
};

export type Localidad = {
  id: string;
  nombre: string;
};

export type Ubicacion = {
  municipio?: {
    id: string;
    nombre: string;
  };
  departamento?: {
    id: string;
    nombre: string;
  };
  provincia?: {
    id: string;
    nombre: string;
  };
};

type GeorefProvinciasResponse = {
  provincias: Array<{ id: string; nombre: string }>;
};

type GeorefDepartamentosResponse = {
  departamentos: Array<{ id: string; nombre: string }>;
};

type GeorefLocalidadesResponse = {
  localidades: Array<{ id: string; nombre: string }>;
};

type GeorefUbicacionResponse = {
  ubicacion: {
    municipio?: { id: string; nombre: string };
    departamento?: { id: string; nombre: string };
    provincia?: { id: string; nombre: string };
  };
};

@Injectable({ providedIn: 'root' })
export class GeorefService {
  private readonly http = inject(HttpClient);

  getProvincias(): Observable<Provincia[]> {
    return this.http
      .get<GeorefProvinciasResponse>('https://apis.datos.gob.ar/georef/api/provincias?campos=id,nombre&max=100')
      .pipe(
        map((res) => {
          const provincias = res.provincias;

          // Separar Ciudad Autónoma de Buenos Aires y Provincia de Buenos Aires
          const caba = provincias.find(
            (p) =>
              p.nombre.toLowerCase().includes('ciudad') &&
              p.nombre.toLowerCase().includes('autónoma')
          );
          const buenosAires = provincias.find((p) => p.nombre === 'Buenos Aires');
          const resto = provincias.filter(
            (p) => p.id !== caba?.id && p.id !== buenosAires?.id
          );

          // Ordenar el resto alfabéticamente
          resto.sort((a, b) => a.nombre.localeCompare(b.nombre, 'es'));

          // Construir array final: CABA, Buenos Aires, resto alfabético
          const resultado: Provincia[] = [];
          if (caba) resultado.push(caba);
          if (buenosAires) resultado.push(buenosAires);
          resultado.push(...resto);

          return resultado;
        })
      );
  }

  getDepartamentos(provinciaId: string): Observable<Departamento[]> {
    return this.http
      .get<GeorefDepartamentosResponse>(
        `https://apis.datos.gob.ar/georef/api/departamentos?provincia=${provinciaId}&campos=id,nombre&max=1000`
      )
      .pipe(
        map((res) => {
          // Ordenar departamentos alfabéticamente
          const departamentos = res.departamentos;
          departamentos.sort((a, b) => a.nombre.localeCompare(b.nombre, 'es'));
          return departamentos;
        })
      );
  }

  getLocalidades(provinciaId: string, departamentoId: string): Observable<Localidad[]> {
    return this.http
      .get<GeorefLocalidadesResponse>(
        `https://apis.datos.gob.ar/georef/api/localidades?provincia=${provinciaId}&departamento=${departamentoId}&campos=id,nombre&max=200`
      )
      .pipe(
        map((res) => {
          // Ordenar localidades alfabéticamente
          const localidades = res.localidades;
          localidades.sort((a, b) => a.nombre.localeCompare(b.nombre, 'es'));
          return localidades;
        })
      );
  }

  getUbicacionPorCoordenadas(lat: number, lng: number): Observable<Ubicacion> {
    return this.http
      .get<GeorefUbicacionResponse>(
        `https://apis.datos.gob.ar/georef/api/ubicacion?lat=${lat}&lon=${lng}`
      )
      .pipe(map((res) => res.ubicacion));
  }

  getMunicipios(provinciaId: string): Observable<{ id: string; nombre: string }[]> {
    return this.http
      .get<{ municipios: Array<{ id: string; nombre: string }> }>(
        `https://apis.datos.gob.ar/georef/api/municipios?provincia=${provinciaId}&campos=id,nombre&max=1000`
      )
      .pipe(
        map((res) => {
          const municipios = res.municipios;
          municipios.sort((a, b) => a.nombre.localeCompare(b.nombre, 'es'));
          return municipios;
        })
      );
  }

  getMunicipioPorId(municipioId: string): Observable<{ id: string; nombre: string }> {
    return this.http
      .get<{ municipios: Array<{ id: string; nombre: string }> }>(
        `https://apis.datos.gob.ar/georef/api/municipios?id=${municipioId}&campos=id,nombre&max=1`
      )
      .pipe(
        map((res) => {
          if (res.municipios && res.municipios.length > 0) {
            return res.municipios[0];
          }
          throw new Error('Municipio no encontrado');
        })
      );
  }
}
