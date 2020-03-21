import { Arena } from "./arena";
import { IGameplayChat, IGameplayDraw } from "../../interfaces/game";

import * as io from 'socket.io';

export class ArenaSolo extends Arena {

    // TODO implement solo

    public start(): void {}

    public receiveInfo(socket: io.Socket, mes: IGameplayChat | IGameplayDraw): void {}

    protected handleGameplayChat(mes: IGameplayChat): void {}

    protected handlePoints(): void {}
}