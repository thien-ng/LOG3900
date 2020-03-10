import { injectable, inject } from 'inversify';
import { UserManagerService } from './user-manager.service';
import 'reflect-metadata';
import * as io from 'socket.io';
import * as http from 'http';
import Types from '../types';
import { ChatService } from './chat.service';
import { IUser } from '../interfaces/user-manager';
import { IReceptMes } from '../interfaces/chat';
import { LobbyManagerService } from '../services/game/lobby-manager.service';
import { GameManagerService } from '../services/game/game-manager.service';
import { IReceptMesLob, IGameplayChat, IGameplayDraw } from '../interfaces/game';
import { AccountDbService } from '../database/account-db.service';

@injectable()
export class WebsocketService {

    private io: io.Server;

    public constructor(
        @inject(Types.UserManagerService) private userServ: UserManagerService,
        @inject(Types.AccountDbService) private accServ: AccountDbService,
        @inject(Types.LobbyManagerService) private lobServ: LobbyManagerService,
        @inject(Types.GameManagerService) private gameServ: GameManagerService,
        @inject(Types.ChatService) private chatServ: ChatService,
    ) { }

    public initWebsocket(server: http.Server): void {
        this.io = io(server);

        this.initSocket();

        this.io.on('connection', (socket: io.Socket) => {
            console.log("connection to socket");

            let username: string;

            socket.on('login', (name: string) => {
                if (this.userServ.checkIfUserIsOnline(name)) {
                    socket.emit("logging", { status: 400, message: `${name} is already connected` });
                } else {
                    socket.emit("logging", { status: 200, message: "logged in successfully" });
                    console.log(name + " logged in");
                    username = name;
                    this.login(username, socket);
                }
            });

            socket.on('chat', (mes: IReceptMes) => {
                this.chatServ.sendMessages(mes);
            });

            socket.on('lobby-chat', (mes: IReceptMesLob) => {
                this.lobServ.sendMessages(mes);
            });

            socket.on('gameplay', (mes: IGameplayChat | IGameplayDraw) => {
                this.gameServ.sendMessageToArena(mes);
            });

            socket.on('logout', () => {
                console.log(username + " logged out");
                this.logout(username);
            });

            socket.on('disconnect', () => {
                console.log(username + " has disconnected");
                this.logout(username);
            });
        });
    }

    private initSocket(): void {
        this.chatServ.setSocket(this.io);
        this.lobServ.initSocketServer(this.io);
        this.gameServ.initSocketServer(this.io);
    }

    private login(username: string, socket: io.Socket): void {
        const user: IUser = { username: username, socketId: socket.id, socket: socket };
        this.userServ.addUser(user);
        this.chatServ.addUserToChannelMap(user);
        this.accServ.logConnection(username, true);
    }

    private logout(username: string): void {
        this.lobServ.handleDisconnect(username);
        this.chatServ.removeUserFromChannelMap(username);
        this.userServ.deleteUser(username);
        this.accServ.logConnection(username, false);
    }
}
