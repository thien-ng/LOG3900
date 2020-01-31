import { injectable, inject } from 'inversify';
import { UserManagerService } from './user-manager.service';
import { ILogin } from '../interfaces/communication';
import 'reflect-metadata';
import * as io from 'socket.io';
import * as http from 'http';
import Types from '../types';
import { ChatService } from './chat.service';
import { IUser } from '../interfaces/user-manager';
import { IReceptMes } from '../interfaces/chat';

@injectable()
export class WebsocketService {

    private io: io.Server;

    public constructor(
        @inject(Types.UserManagerService) private userServ: UserManagerService,
        @inject(Types.ChatService) private chatServ: ChatService,
        ) {}

    public initWebsocket(server: http.Server): void {
        this.io = io(server);

        this.initSocket();
        
        // event is called when client connects
        this.io.on('connection', (socket: io.Socket) => {
            let username: string;
            
            // test event to check if socket is on
            socket.on('login', (mes: ILogin) => {                
                username = mes.username;
                this.login(username, socket);
            });

            socket.on('chat', (mes: IReceptMes) => {
                this.chatServ.sendMessages(mes);
            });

            // event is called when client disconnects
            socket.on('disconnect', () => {
                this.logout(username);
            });
        });
    }

    private initSocket(): void {
        this.chatServ.setSocket(this.io);
    }

    private login(username: string, socket: io.Socket): void {       
        const user: IUser = {username: username, socketId: socket.id};
        this.userServ.addUser(user);
        this.chatServ.addUserToChannelMap(user);
    }

    private logout(username: string): void {
        this.userServ.deleteUser(username);
        this.chatServ.removeUserFromChannelMap(username);
    }
}