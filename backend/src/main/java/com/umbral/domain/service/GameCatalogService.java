package com.umbral.domain.service;

import com.umbral.domain.dto.GameResponse;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class GameCatalogService {
    public List<GameResponse> getAllGames(){

        List<GameResponse> games = new ArrayList<>();
        games.add(new GameResponse(
                1L,
                "Persona 5 Royal",
                "Persona 5 Royal es un juego de rol japonés donde eres un estudiante de secundaria en Tokio que vive una doble vida. De día estudias y haces amigos; de noche te conviertes en un ladrón fantasma que roba los malos deseos de las mentes corruptas"
        ));
        return games;
    }
}
