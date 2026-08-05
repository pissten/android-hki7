FROM nginx:1.29-alpine

COPY webApp/nginx.conf /etc/nginx/conf.d/default.conf
COPY webApp/build/dist/wasmJs/productionExecutable/ /usr/share/nginx/html/

EXPOSE 80
