interface IMessage {
    content:    string,
    time:       string,
}

export interface IChannelMessageDB extends IMessage {
    channel_id: string,
    account_id: number,
}

export interface IChannelMessageReq extends IMessage {
    username: string,
}

export interface IReceptMes {
    username:   string,
    channel_id: string,
    content:    string,
}

export interface IEmitMes extends IReceptMes {
    time: string,
}

export interface IChannelIds {
    id: string
}

export interface ISearchChannel extends IChannelIds {
    sub: boolean
}