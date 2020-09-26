import java.util.ArrayList;
import java.util.Random;

import tester.*;
import javalib.impworld.*;
import java.awt.Color;
import javalib.worldimages.*;

//Game class that represents the game and its operations and visualization
class Game extends World {
  ArrayList<ArrayList<Cell>> yList;
  ArrayList<Cell> xList;
  int mines;
  int minesPlaced;
  int revealedCells = 0;
  Random rand;

  Game() {
    yList = new ArrayList<ArrayList<Cell>>(16);
    xList = new ArrayList<Cell>(30);

    for (int i = 0; i < 16; i++) {
      yList.add(i, new ArrayList<Cell>(30));
      for (int j = 0; j < 30; j++) {
        yList.get(i).add(j, new Cell());
      }
    }

    mines = 99;
    minesPlaced = 0;
    rand = new Random();
  }

  Game(int xSize, int ySize, int mines, Random r) {
    yList = new ArrayList<ArrayList<Cell>>(ySize);
    xList = new ArrayList<Cell>(xSize);

    for (int i = 0; i < ySize; i++) {
      yList.add(i, new ArrayList<Cell>(xList.size()));
      for (int j = 0; j < xSize; j++) {
        yList.get(i).add(j, new Cell());
      }
    }

    this.mines = mines;
    minesPlaced = 0;
    rand = r;
  }

  /*Fields:
   *  this.yList ... ArrayList<ArrayList<Cell>>
   *  this.xList ... ArrayList<Cell>
   *  this.mines ... int
   *  this.minesPlaced ... int
   *  this.rand ... Random
   *  
   *Methods:
   *  this.startGame ... void
   *  this.placeMines ... void
   *  this.makeScene ... WorldScene
   */

  //initializes the neighbor list for the cell to all adjacent cells
  void startGame() {
    for (int yAxis = 0; yAxis < yList.size(); yAxis++) {
      for (int xAxis = 0; xAxis < yList.get(yAxis).size(); xAxis++) {

        Cell currentCell = yList.get(yAxis).get(xAxis);

        // go through the 8 surrounding cells and add this cell as a neighbor
        for (int yDiff = -1; yDiff <= 1; yDiff++) {
          for (int xDiff = -1; xDiff <= 1; xDiff++) {

            if ((yDiff + yAxis) >= 0
                && (yDiff + yAxis) < yList.size()
                && (xDiff + xAxis) >= 0 
                && (xDiff + xAxis) < yList.get(yAxis).size()) {

              yList.get(yDiff + yAxis).get(xDiff + xAxis).neighbors.add(currentCell); 

            }
          }
        }

        currentCell.neighbors.remove(currentCell);
      }
    }

  }

  //places the mines on the board in a random distribution
  void placeMines() {

    while (minesPlaced < mines) {

      int randX = rand.nextInt(yList.get(0).size());
      int randY = rand.nextInt(yList.size());

      Cell currentCell = yList.get(randY).get(randX);



      if (!currentCell.isBomb) {

        currentCell.isBomb = true;
        yList.get(randY).set(randX, currentCell);
        this.minesPlaced += 1;


        // update the 8 surrounding cells to show the increase in adjacent bombs
        for (int i = -1; i <= 1; i++) {
          for (int j = -1; j <= 1; j++) {

            if ((randY + i) >= 0
                && (randY + i) < yList.size()
                && (randX + j) >= 0 
                && (randX + j) < yList.get(randY).size()) {
              yList.get(randY + i).get(randX + j).adjacentBombs++; 
            }
          }

        }

      }

    }

  }

  public void onMouseClicked(Posn loc, String button) {

    Cell currentCell = yList.get(loc.y / 20).get(loc.x / 20);


    if (button.equals("LeftButton")) {
      currentCell.isVisible = true;
      revealedCells++;

      if (currentCell.adjacentBombs == 0) {
        floodFill(yList.get(loc.y / 20).get(loc.x / 20));
      }
      else if (currentCell.isBomb) {

        for (int yAxis = 0; yAxis < yList.size(); yAxis++) {
          for (int xAxis = 0; xAxis < yList.get(yAxis).size(); xAxis++) {
            if (yList.get(yAxis).get(xAxis).isBomb) {
              yList.get(yAxis).get(xAxis).isVisible = true;
            }
          }
        }

        this.makeScene();
        this.endOfWorld("Game Over");
      }
      else if (revealedCells == yList.size() * yList.get(0).size() - this.mines) {
        
        this.endOfWorld("You Win!");
      }

    }
    else if (button.equals("RightButton")) {
      currentCell.flagged = !currentCell.flagged;
    }
  }

  //method for visualizing the game board
  public WorldScene makeScene() {

    WorldScene bg = this.getEmptyScene();

    for (int i = 0; i < yList.size(); i++) {
      for (int j = 0; j < yList.get(i).size(); j++) {
        bg.placeImageXY(yList.get(i).get(j).draw(), j * 20 + 10, i * 20 + 10);
      }
    }

    return bg;
  }

  public void randomStart() {

    boolean areaOpened = false;

    while (!areaOpened) {
      int randX = rand.nextInt(yList.get(0).size());
      int randY = rand.nextInt(yList.size());

      Cell currentCell = yList.get(randY).get(randX);

      if (currentCell.adjacentBombs == 0) {
        revealedCells++;
        currentCell.isVisible = true;
        areaOpened = true;
        floodFill(currentCell);

      }

    }
  }

  void floodFill(Cell currentCell) {

    for (int i = 0; i < currentCell.neighbors.size(); i++) {

      if (currentCell.neighbors.get(i).adjacentBombs == 0 && !currentCell.neighbors.get(i).isVisible) {
        currentCell.neighbors.get(i).isVisible = true;
        floodFill(currentCell.neighbors.get(i));
      }
      else {
        currentCell.neighbors.get(i).isVisible = true;
      }

    }

  }


}

//Class for the individual cells in the game
class Cell {
  int adjacentBombs;
  boolean isBomb;
  boolean flagged = false;
  boolean isVisible;
  Color textColor = Color.GRAY;
  ArrayList<Cell> neighbors;

  Cell() {
    adjacentBombs = 0;
    isBomb = false;
    isVisible = false;
    neighbors = new ArrayList<Cell>();
  }

  Cell(int n, boolean bom, boolean vis) {
    adjacentBombs = n;
    isBomb = bom;
    isVisible = vis;
    neighbors = new ArrayList<Cell>();
  }

  /*Fields:
   *  this.adjacentBombs ... int
   *  this.isBomb ... boolean
   *  this.isVisible ... boolean
   *  this.textColor ... Color
   *  this.neighbors ... ArrayList<Cell>
   *  
   *Methods:
   *  this.cellColor ... void
   *  this.draw ... WorldImage
   */


  //method for determining what color the cells should be
  void cellColor() {

    if (adjacentBombs == 1) {
      textColor = Color.BLUE;
    }
    else if (adjacentBombs == 2) {
      textColor = Color.GREEN;
    }
    else if (adjacentBombs == 3) {
      textColor = Color.RED;
    }
    else if (adjacentBombs == 4) {
      textColor = Color.MAGENTA;
    }
    else if (adjacentBombs == 5) {
      textColor = new Color(180, 0, 0); // maroon
    }
    else if (adjacentBombs == 6) {
      textColor = new Color(64, 220, 208); // turqoise
    }
    else if (adjacentBombs == 7) {
      textColor = Color.BLACK;
    }
    else {
      textColor = Color.WHITE;
    }


  }

  //method for visualizing the cells in the game board
  WorldImage draw() {

    cellColor();

    if (flagged) {
      return new OverlayImage(
          new OverlayImage(new RotateImage(new EquilateralTriangleImage(10, OutlineMode.SOLID, Color.RED), -90).movePinhole(1.5, 3), 
              new RectangleImage(3, 16, OutlineMode.SOLID, Color.BLACK).movePinhole(-3, 0)).movePinhole(0, -1),
          new OverlayImage(new RectangleImage(20, 20, OutlineMode.OUTLINE, Color.BLACK),
              new RectangleImage(20, 20, OutlineMode.SOLID, new Color(161, 187, 240))));
    }
    else if (isVisible) {
      if (isBomb) {
        return new OverlayImage(
            new CircleImage(7, OutlineMode.SOLID, Color.BLACK),
            new OverlayImage(new RectangleImage(20, 20, OutlineMode.OUTLINE, Color.BLACK),
                new RectangleImage(20, 20, OutlineMode.SOLID, Color.WHITE)));
      }
      else {
        return new OverlayImage(
            new TextImage(Integer.toString(adjacentBombs), 17, FontStyle.BOLD, textColor),
            new OverlayImage(new RectangleImage(20, 20, OutlineMode.OUTLINE, Color.BLACK),
                new RectangleImage(20, 20, OutlineMode.SOLID, Color.WHITE)));
      }
    } 
    else {
      return new OverlayImage(
          new RectangleImage(20, 20, OutlineMode.OUTLINE, Color.BLACK),
          new RectangleImage(20, 20, OutlineMode.SOLID, new Color(161, 187, 240)));
    }


  }

}

//class for testing the game and cell class
class ExamplesGame {


  Game testGame = new Game();  
  Game testGameMore = new Game(30, 16, 200, new Random());
  Game testGameLess = new Game(30, 16, 20, new Random());

  Game gameSmall = new Game(2, 2, 1, new Random());
  Game gameMedium = new Game(2, 3, 2, new Random());
  Game gameBig = new Game(3, 3, 2, new Random());

  ArrayList<Cell> tGCell = new ArrayList<Cell>();

  Cell cell0 = new Cell();
  Cell cell1 = new Cell(1, true, true);
  Cell cell2 = new Cell(2, false, true);
  Cell cell5 = new Cell(5, true, false);
  Cell cell6 = new Cell(6, false, false);

  //method to help test startGame gets the neighbors of an edge cell
  ArrayList<Cell> neighborsAdder(ArrayList<Cell> l, Game testG) {
    l.add(testG.yList.get(0).get(1));
    l.add(testG.yList.get(1).get(0));
    l.add(testG.yList.get(1).get(1));
    return l;
  }

  //method to test the start game method
  void testStartGame(Tester t) {
    t.checkExpect(testGame.yList.get(0).get(0).neighbors, new ArrayList<Cell>());
    t.checkExpect(testGame.yList.get(1).get(1).neighbors, new ArrayList<Cell>());
    testGame.startGame();
    t.checkExpect(testGame.yList.get(0).get(0).neighbors, 
        this.neighborsAdder(new ArrayList<Cell>(), testGame));
    t.checkExpect(testGameMore.yList.get(0).get(0).neighbors, new ArrayList<Cell>());
    testGameMore.startGame();
    t.checkExpect(testGameMore.yList.get(0).get(0).neighbors, 
        this.neighborsAdder(new ArrayList<Cell>(), testGameMore));
    t.checkExpect(testGameLess.yList.get(0).get(0).neighbors, new ArrayList<Cell>());
    testGameLess.startGame();
    t.checkExpect(testGameLess.yList.get(0).get(0).neighbors, 
        this.neighborsAdder(new ArrayList<Cell>(), testGameLess));   
  }


  //method to test placeMines method
  void testPlaceMines(Tester t) {
    t.checkExpect(testGame.minesPlaced, 0);
    testGame.placeMines();
    t.checkExpect(testGame.minesPlaced, 99);
    t.checkExpect(testGameMore.mines, 200);
    t.checkExpect(testGameMore.minesPlaced, 0);
    testGameMore.placeMines();
    t.checkExpect(testGameMore.minesPlaced, 200);
    t.checkExpect(testGameLess.mines, 20);
    t.checkExpect(testGameLess.minesPlaced, 0);
    testGameLess.placeMines();
    t.checkExpect(testGameLess.minesPlaced, 20);  
  }

  //method to test cellColor method
  void testTextColor(Tester t) {
    cell0 = new Cell();
    cell1 = new Cell(1, true, true);
    cell5 = new Cell(5, true, false);
    cell6 = new Cell(6, false, false);
    t.checkExpect(cell0.textColor, Color.GRAY);
    cell0.cellColor();
    t.checkExpect(cell0.textColor, Color.WHITE);
    t.checkExpect(cell1.textColor, Color.GRAY);
    cell1.cellColor();
    t.checkExpect(cell1.textColor, Color.BLUE);
    t.checkExpect(cell5.textColor, Color.GRAY);
    cell5.cellColor();
    t.checkExpect(cell5.textColor, new Color(180, 0, 0));
    t.checkExpect(cell6.textColor, Color.GRAY);
    cell6.cellColor();
    t.checkExpect(cell6.textColor, new Color(64, 220, 208));

  }

  //method to test the draw function
  boolean testDraw(Tester t) {
    return t.checkExpect(cell0.draw(), new OverlayImage(
        new RectangleImage(20, 20, OutlineMode.OUTLINE, Color.BLACK),
        new RectangleImage(20, 20, OutlineMode.SOLID, new Color(161, 187, 240))))
        && t.checkExpect(cell1.draw(), new OverlayImage(
            new CircleImage(7, OutlineMode.SOLID, Color.BLACK),
            new OverlayImage(new RectangleImage(20, 20, OutlineMode.OUTLINE, Color.BLACK),
                new RectangleImage(20, 20, OutlineMode.SOLID, Color.WHITE))))
        && t.checkExpect(cell6.draw(), new OverlayImage(
            new RectangleImage(20, 20, OutlineMode.OUTLINE, Color.BLACK),
            new RectangleImage(20, 20, OutlineMode.SOLID, new Color(161, 187, 240))));

  }

  //method to help test the make scene function on the med world
  WorldScene buildSceneTestMedWorld() {
    WorldScene es = gameMedium.getEmptyScene();
    es.placeImageXY(gameMedium.yList.get(0).get(0).draw(), 10, 10);
    es.placeImageXY(gameMedium.yList.get(0).get(1).draw(), 30, 10);
    es.placeImageXY(gameMedium.yList.get(1).get(0).draw(), 10, 30);
    es.placeImageXY(gameMedium.yList.get(1).get(1).draw(), 30, 30);
    es.placeImageXY(gameMedium.yList.get(2).get(0).draw(), 10, 50);
    es.placeImageXY(gameMedium.yList.get(2).get(1).draw(), 30, 50);
    return es;
  }

  //method to help test the make scene function on the small world
  WorldScene buildSceneTestSmallWorld() {
    WorldScene es = gameMedium.getEmptyScene();
    es.placeImageXY(gameMedium.yList.get(0).get(0).draw(), 10, 10);
    es.placeImageXY(gameMedium.yList.get(0).get(1).draw(), 30, 10);
    es.placeImageXY(gameMedium.yList.get(1).get(0).draw(), 10, 30);
    es.placeImageXY(gameMedium.yList.get(1).get(1).draw(), 30, 30);
    return es;
  }

  //method to help test the make scene function on the big world
  WorldScene buildSceneTestBigWorld() {
    WorldScene es = gameBig.getEmptyScene();
    es.placeImageXY(gameBig.yList.get(0).get(0).draw(), 10, 10);
    es.placeImageXY(gameBig.yList.get(0).get(1).draw(), 30, 10);
    es.placeImageXY(gameBig.yList.get(0).get(2).draw(), 50, 10);
    es.placeImageXY(gameBig.yList.get(1).get(0).draw(), 10, 30);
    es.placeImageXY(gameBig.yList.get(1).get(1).draw(), 30, 30);
    es.placeImageXY(gameBig.yList.get(1).get(2).draw(), 50, 30);
    es.placeImageXY(gameBig.yList.get(2).get(0).draw(), 10, 30);
    es.placeImageXY(gameBig.yList.get(2).get(1).draw(), 30, 30);
    es.placeImageXY(gameBig.yList.get(2).get(2).draw(), 50, 50);
    return es;
  }

  //method to test make scene
  boolean testMakeScene(Tester t) {
    gameSmall.startGame();
    gameSmall.placeMines();
    return t.checkExpect(gameSmall.makeScene(), this.buildSceneTestSmallWorld())
        && t.checkExpect(gameMedium.makeScene(), this.buildSceneTestMedWorld())
        && t.checkExpect(gameBig.makeScene(), this.buildSceneTestBigWorld());
  }

  void testGame(Tester t) {
    Game g = new Game();
    g.startGame();
    g.placeMines();
    g.randomStart();
    g.bigBang(600, 320);
  }

}