import { IDrawingCreator } from "../../../interfaces/creator";
import { IDrawing, Format, Type } from "../../../interfaces/game";

export class DrawingTools {

    public static prepareGameRule(strokes: IDrawingCreator[]): IDrawing[] {
        const drawings: IDrawing[] = [];

        strokes.forEach(stroke => {
            
            const w: number = stroke.width;

            let startX = stroke.points[0].x;
            let startY = stroke.points[0].y;
            let endX;
            let endY;

            for (let i = 1; i < stroke.points.length; i++) {
                const isEnd = (i === stroke.points.length - 1);

                endX = stroke.points[i].x;
                endY = stroke.points[i].y;

                const drawing: IDrawing = this.buildDrawing(stroke.color, w, startX, startY, endX, endY, isEnd);
                
                drawings.push(drawing);

                startX = stroke.points[i].x;
                startY = stroke.points[i].y;
            }

        });

        return drawings;
    }

    private static buildDrawing(col: string, w: number, sx: number, sy: number, ex: number, ey: number, end: boolean): IDrawing {
        return {
            startPosX: sx,
            startPosY: sy,
            endPosX:   ex,
            endPosY:   ey,
            color:     col,
            width:     w,
            isEnd:     end,
            format:    Format.circle,
            type:      Type.ink,
        }
    }

}