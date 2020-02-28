import { injectable, inject } from 'inversify';
import { UserManagerService } from './user-manager.service';
import 'reflect-metadata';
import * as io from 'socket.io';
import * as http from 'http';
import Types from '../types';
import { ChatService } from './chat.service';
import { IUser } from '../interfaces/user-manager';
import { IReceptMes } from '../interfaces/chat';
import { GameConnection } from '../services/game/game-connection.service';

@injectable()
export class WebsocketService {

    private io: io.Server;

    public constructor(
        @inject(Types.UserManagerService) private userServ: UserManagerService,
        @inject(Types.GameConnection) private gameConnectServ: GameConnection,
        @inject(Types.ChatService) private chatServ: ChatService,
        ) {}

    public initWebsocket(server: http.Server): void {
        this.io = io(server);

        this.initSocket();
        
        // event is called when client connects
        this.io.on('connection', (socket: io.Socket) => {
            console.log("connection to socket");
            
            let username: string;
            
            // test event to check if socket is on
            socket.on('login', (name: string) => {    
                if (this.userServ.checkIfUserIsOnline(name)) {
                    socket.emit("logging", {status: 400, message: `${name} is already connected`});
                } else {
                    socket.emit("logging", {status: 200, message: "logged in successfully"});
                    console.log(name + " logged in");
                    username = name;
                    this.login(username, socket);
                }
            });

            socket.on('chat', (mes: IReceptMes) => {
                this.chatServ.sendMessages(mes);
            });

            socket.on('logout', () => {
                console.log(username + " logged out");
                this.logout(username);
            });

            // event is called when client disconnects
            socket.on('disconnect', () => {
                console.log(username + " has disconnected");
                this.logout(username);
            });
        });
    }

    private initSocket(): void {
        this.chatServ.setSocket(this.io);
        this.gameConnectServ.initSocketServer(this.io);
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