import * as io from 'socket.io';

export interface IUser {
    username: string,
    socketId: string,
    socket?: io.Socket,
}

export interface IUserId {
    id: number;
}
