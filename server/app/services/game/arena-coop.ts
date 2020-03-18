import { Arena } from "./arena";
import { IGameplayChat, IGameplayDraw } from "../../interfaces/game";

import * as io from 'socket.io';

export class ArenaCoop extends Arena {

    // TODO implement coop

    public start(): void {}

    public receiveInfo(socket: io.Socket, mes: IGameplayChat | IGameplayDraw): void {}

}