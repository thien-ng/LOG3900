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

export interface IInvitationChannel {
    message: string,
    channel: string,
}