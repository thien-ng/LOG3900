export interface IRegistration {
    username: string,
    password: string,
}

export interface ILogin {
    username: string,
    password: string,
}

export interface IStatus {
    status:  number;
    message: string;
}

export interface IMessage {
    type:       IMessageType,
    content:    string | ILogin,
}

export enum IMessageType {
    login,
    chat,
}