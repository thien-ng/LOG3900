import { createCanvas } from "canvas";
import { categories } from "./categories";
import { ISuggestion, IPoint } from "../../../interfaces/creator";
import { IDrawingCreator } from "../../../interfaces/creator";

const format = require('string-format');
const axios = require('axios');

const URL = "https://quickdrawfiles.appspot.com/drawing/{0}?id={1}&key=AIzaSyC0U3yLy_m6u7aOMi9YJL2w1vWG4oI5mj0";
const dataSetPoolSize = 25;

export class CreationAssist2 {

    public static async fetchSuggestion(): Promise<ISuggestion> {
        
        const category = categories[this.generateRandom(345)];
        const idIndex = this.generateRandom(dataSetPoolSize);
        const url = format(URL, category, idIndex);
        const data = (await axios.get(url)).data
        
        const canvas = createCanvas(300, 300);
        const ctx = canvas.getContext("2d");
        
        ctx.lineJoin = ctx.lineCap = 'round';
        ctx.lineWidth = 4;
        ctx.strokeStyle = '#000000';
        ctx.beginPath();
        
        const drawings: IDrawingCreator[] = [];
        
        for(var i = 0; i < data.drawing.length; i++){
            var len = data.drawing[i][0].length;

            let points: IPoint[] = [];

            for(var j = 0; j < len; j++){
                
                const x = data.drawing[i][0][j];
                const y = data.drawing[i][1][j];
                
                if(j === 0)
                    ctx.moveTo(x, y);
                if(j > 0)
                    ctx.lineTo(x, y);
                    
                points.push({x: x*3 + 120, y: y*3 + 120});
            }
            ctx.stroke();
            points = this.dilate(points);
            drawings.push({color: "#FF000000", width: 10, points: points });
        }

        return {
            drawPng: canvas.toDataURL("image/png").replace("image/png", "image/octet-stream"),
            drawPxl: drawings,
            object: category,
        }
    }

    private static dilate(pts: IPoint[]): IPoint[] {
        const newPts = [];

        let prevPts = pts[0];
        for (let i = 1; i < pts.length; i++) {
            const tempPts = pts[i];

            const eq = this.calculateEquation(prevPts, tempPts);

            //if point prev is left side of temp
            if (prevPts.x - tempPts.x <= 0) {
                let x = prevPts.x;
                while (x < tempPts.x) {
                    const y = this.findY(eq[0], eq[1], x);
                    newPts.push({x: x, y: y});
                    x += 10;
                }
            }
            //if point prev is right side of temp
            else {
                let x = prevPts.x;
                while (x > tempPts.x) {
                    const y = this.findY(eq[0], eq[1], x);
                    newPts.push({x: x, y: y});
                    x -= 10;
                }
            }

            newPts.push(prevPts);
            prevPts = tempPts;
        }

        return newPts;
    }

    private static calculateEquation(pt1: IPoint, pt2: IPoint): [number, number] {
        const m = (pt2.y-pt1.y)/(pt2.x-pt1.x);
        const b = Math.floor(pt2.y - (m * pt2.x));
        return [m, b];
    }

    private static findY(m: number, b: number, x: number): number {
        return (m*x) + b;
    }

    private static generateRandom(max: number): number {
        return Math.floor((Math.random() * max));
    }

}