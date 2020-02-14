import { NextFunction, Request, Response, Router } from 'express';
import { inject, injectable } from 'inversify';
import Types from '../types';
import { ChatService } from '../services/chat.service';


@injectable()
export class ChatController {
    public router: Router;

    public constructor(@inject(Types.ChatService) private chatServ: ChatService) {
        this.configureRouter();
    }

    private configureRouter() {
        this.router = Router();

        this.router.get('/messages/:id', async (req: Request, res: Response, next: NextFunction) => {
            res.json(await this.chatServ.getMessagesWithChannelId(req.params.id));
        });
        
        this.router.get('/channels/all', async (req: Request, res: Response, next: NextFunction) => {
            res.json(await this.chatServ.getAllExistingChannels());
        });

        this.router.get('/channels/:username', async (req: Request, res: Response, next: NextFunction) => {
            res.json(await this.chatServ.getChannelsWithAccountName(req.params.username));
        });

        this.router.put('/channels/join/:username/:channel', async (req: Request, res: Response, next: NextFunction) => {
            res.json(await this.chatServ.joinChannel(req.params.username, req.params.channel));
        });

        this.router.delete('/channels/leave/:username/:channel', async (req: Request, res: Response, next: NextFunction) => {
            res.json(await this.chatServ.leaveChannel(req.params.username, req.params.channel));
        });

        this.router.post('/channels/invite', async (req: Request, res: Response, next: NextFunction) => {
            res.json(await this.chatServ.sendInviteToChannel(req.body.inviter, req.body.invitee, req.body.channel));
        });

    }
}
