import { injectable, inject } from 'inversify';
import { UserManagerService } from './user-manager.service';
import { IMessage, IMessageType, ILogin } from '../interfaces/communication';
import 'reflect-metadata';
import Types from '../types';
import * as ws from 'ws';
import * as http from 'http';

@injectable()
export class WebsocketService {

    private ws: ws.Server;

    public constructor(
        @inject(Types.UserManagerService) private users: UserManagerService
        ) {}

    public initWebsocket(server: http.Server): void {
        this.ws = new ws.Server({ server });
        
        // event is called when client connects
        this.ws.on('connection', (socket: ws) => {
            let username: string;

            // test event to check if socket is on
            socket.on('message', (mes: IMessage) => {
                if (mes.type === IMessageType.login) {
                    username = (mes.content as ILogin).username;
                    this.login(username, socket);
                }
            });

            // event is called when client disconnects
            socket.on('close', () => {
                this.users.deleteUser(username);
            });
        });
    }

    private login(username: string, socket: ws): void {
        this.users.addUser(username, socket);
    }
}