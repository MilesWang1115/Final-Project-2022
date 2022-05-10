public enum PiecesAction {
    Move, 		//走子
    Capture, 	//吃子
    Castling, 	//王车易位
    Check, 		//将军
    Promotion,	//兵升变
    Illegal,	//犯规

    //These actions are design for Class Result
    Checkmate,  //将杀
    Stalemate,  //无子可动
    PerpetualCheck,	//长将
    Repetition,	//重复局面
}

