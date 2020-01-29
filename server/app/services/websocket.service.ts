import { injectable } from 'inversify';
import 'reflect-metadata';
import * as io from 'socket.io';
import * as http from 'http';

@injectable()
export class WebsocketService {

    private io: io.Server;

    public initWebsocket(server: http.Server): void {
        this.io = io(server);
        
        // event is called when client connects
        this.io.on('connection', (socket) => {
            console.log("test");
            
            // test event to check if socket is on
            socket.on('message', (data) => {
                socket.send(data);
            });

            // event is called when client disconnects
            socket.on('disconnect', () => {
                console.log("disconnected");
            });
        });
    }
}