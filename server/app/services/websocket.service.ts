import { injectable } from 'inversify';
import 'reflect-metadata';
import * as ws from 'ws';
import * as http from 'http';

@injectable()
export class WebsocketService {

    private ws: ws.Server;

    public initWebsocket(server: http.Server): void {
        this.ws = new ws.Server({ server });
        
        // event is called when client connects
        this.ws.on('connection', (socket) => {

            // test event to check if socket is on
            socket.on('message', (data) => {
                socket.send(data);
            });

            // event is called when client disconnects
            socket.on('close', () => {
                console.log("disconnected");
            });
        })

    }
}