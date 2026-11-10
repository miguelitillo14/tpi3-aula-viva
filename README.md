# TPI 3: Acceso a datos en Aula Viva

Prototipo académico para estudiar el acceso de una aplicación Java a PostgreSQL mediante JDBC y una vista externa de solo lectura. Los datos son sintéticos y no proceden del campus.

## Requisitos

- Docker Desktop iniciado y Docker Compose v2.
- Conexión a Internet durante el primer arranque para descargar imágenes y dependencias Maven.

Java, Maven y PostgreSQL se ejecutan en contenedores; no hace falta instalarlos localmente.

## Ejecutar

Abre PowerShell en esta carpeta y ejecuta:

    docker compose up --build --abort-on-container-exit

El primer arranque puede tardar porque descarga PostgreSQL y compila el cliente Java. La consola debe mostrar cuatro espacios. Para consultar solo A101:

    docker compose run --rm app A101

Para cerrar los contenedores:

    docker compose down

La base inicializada persiste en un volumen. Para borrar ese volumen local y repetir la carga inicial:

    docker compose down -v
    docker compose up --build --abort-on-container-exit

El parámetro -v elimina los datos locales de demostración.

## Resultado esperado

La consulta completa devuelve un estado por espacio: R002/A101, R007/A102, R006/B201 y R005/C101. El filtro A101 devuelve R002 con ocupación 0,8, ruido 50 dB y 6 plazas estimadas disponibles. Para A102 el estado vigente es R007, ocupación 0,25 y 15 plazas estimadas disponibles.

## Qué demuestra

- app.space y app.report forman el nivel conceptual, con clave primaria, clave foránea y restricciones.
- app.vw_student_space_status es el nivel externo que consume el cliente.
- Los tipos PostgreSQL y el índice B-tree pertenecen a la implementación interna.
- Java conecta mediante JDBC y filtra con PreparedStatement.
- El rol aula_reader puede consultar la vista, pero no las tablas base.

JDBC es el mecanismo implementado. ODBC y Hibernate se describen en la memoria como alternativas y no se ejecutan en este prototipo. Las contraseñas del ejemplo son valores locales de demostración; no corresponden a cuentas reales ni deben reutilizarse en producción.

## Archivos

- main.tex: memoria basada en la plantilla del profesor.
- db/init/001_schema.sql: esquema, datos, vista y permisos.
- src/main/java/es/uloyola/aulaviva/Main.java: cliente JDBC.
- pom.xml, Dockerfile, docker-compose.yml: compilación y ejecución.
- logo_uloyola.png: logotipo necesario para compilar la memoria.
- TPI3.pdf: memoria final lista para entregar.

