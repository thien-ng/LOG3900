import { injectable, inject } from 'inversify';
import { UserManagerService } from './user-manager.service';
import { ILogin } from '../interfaces/communication';
import 'reflect-metadata';
import * as io from 'socket.io';
import * as http from 'http';
import Types from '../types';

@injectable()
export class WebsocketService {

    private io: io.Server;

    public constructor(
        @inject(Types.UserManagerService) private users: UserManagerService
        ) {}

    public initWebsocket(server: http.Server): void {
        this.io = io(server);
        
        // event is called when client connects
        this.io.on('connection', (socket: io.Socket) => {
            let username: string;
            
            // test event to check if socket is on
            socket.on('login', (mes: ILogin) => {
                username = mes.username;
                this.login(username, socket);
            });

            // event is called when client disconnects
            socket.on('disconnect', () => {
                this.users.deleteUser(username);
            });
        });
    }

    private login(username: string, socket: io.Socket): void {
        this.users.addUser({username: username, socketId: socket.id});
    }
}