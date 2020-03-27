import { Arena } from "./arena";
import { IGameplayChat, IGameplayDraw, IGameplayReady } from "../../interfaces/game";

import * as io from 'socket.io';

export class ArenaSolo extends Arena {

    // TODO implement solo

    public start(): void {}

    public receiveInfo(socket: io.Socket, mes: IGameplayChat | IGameplayDraw | IGameplayReady): void {}

    protected handleGameplayChat(mes: IGameplayChat): void {}

    protected handlePoints(): void {}

    protected startBotDrawing(botName: string, arenaTime: number): NodeJS.Timeout {return setInterval(() =>{}, 1)}

    protected botAnnounceStart(): void {}
    
    protected botAnnounceEndSubGane(): void {}
}