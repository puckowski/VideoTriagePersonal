package com.keypointforensics.videotriage.chart;

import java.util.ArrayList;
import java.util.Map.Entry;

import com.keypointforensics.videotriage.util.MapUtils;

public class BlobTrackingHeatMapChartBuilder extends VideoTriageChartBuilder {
	
	private final int HEAT_MAP_TILE_GRID_WIDTH  = 20;
	private final int HEAT_MAP_TILE_GRID_HEIGHT = 20;
	
	private final String JAVASCRIPT_FILENAME_SHORT = "blob_tracking_heat_map.js";
	private final String UNIQUE_DIV_NAME           = "BlobTrackingHeatMap";
	
	private ArrayList<Integer> mCoordXList;
	private ArrayList<Integer> mCoordYList;
	
	private StringBuilder mChartBuilder;
	private StringBuilder mHtmlBuilder;
			
	private int mMinX;
	private int mMinY;
	
	private int mMaxX;
	private int mMaxY;
	
	private HeatMapTile[][] mHeatMapTileGrid;
	
	public BlobTrackingHeatMapChartBuilder(final String reportRoot, final String reportPageHeader) {
		super(reportRoot, reportPageHeader);
		
		mCoordXList = new ArrayList<Integer>();
		mCoordYList = new ArrayList<Integer>();
		mChartBuilder  = new StringBuilder(1000);
		mHtmlBuilder   = new StringBuilder(2000);
						
		prepareNewTimeSeriesChart();
	}
	
	public void setJavaScriptFilenameShort() {
		mJavaScriptFilenameShort = JAVASCRIPT_FILENAME_SHORT;
	}

	private void prepareNewTimeSeriesChart() {
		mChartBuilder.append("var layout={margin:{r:180,t:40,b:140,l:100},title:'Object Tracking Heat Map',xaxis:{title:'X Coordinate',tickangle:45},yaxis:{title:'Y Coordinate'}};var data=[{type:'heatmap',z:[");
	}
	
	public void addEvent(final String coordEvent) {		
		final int indexOfSpace = coordEvent.indexOf(" ");
		int x = Integer.parseInt(coordEvent.substring(0, indexOfSpace));
		int y = Integer.parseInt(coordEvent.substring(indexOfSpace + 1, coordEvent.length()));
				
		mCoordXList.add(x);
		mCoordYList.add(y);
		
		if(mMinX > x) {
			mMinX = x;
		}
		
		if(mMinY > y) {
			mMinY = y;
		}
		
		if(mMaxX < x) {
			mMaxX = x;
		}
		
		if(mMaxY < y) {
			mMaxY = y;
		}
	}
	
	private void calculateTileWeights() {		
		int xRange = mMaxX - mMinX;
		int yRange = mMaxY - mMinY;
		
		int xTileSize = xRange / HEAT_MAP_TILE_GRID_WIDTH;
		int yTileSize = yRange / HEAT_MAP_TILE_GRID_HEIGHT;
		
		mHeatMapTileGrid = new HeatMapTile[HEAT_MAP_TILE_GRID_WIDTH][HEAT_MAP_TILE_GRID_HEIGHT];
		HeatMapTile heatMapTile;
		
		int startXBound = 0;
		int startYBound = 0;
		
		int endXBound = xTileSize;
		int endYBound = yTileSize;
		
		int i, n;
		
		for(i = 0; i < HEAT_MAP_TILE_GRID_HEIGHT; ++i) {
			for(n = 0; n < HEAT_MAP_TILE_GRID_WIDTH; ++n) {
				heatMapTile = new HeatMapTile(startXBound, startYBound, endXBound, endYBound);
				mHeatMapTileGrid[i][n] = heatMapTile;
				
				startXBound += xTileSize;
				endXBound += xTileSize;
			}
			
			startXBound = 0;
			endXBound = xTileSize;
			
			startYBound += yTileSize;
			endYBound += yTileSize;
		}
		
		int x, y;
		int coordIndex;
		
		for(i = 0; i < HEAT_MAP_TILE_GRID_HEIGHT; ++i) {
			for(n = 0; n < HEAT_MAP_TILE_GRID_WIDTH; ++n) {
				for(coordIndex = 0; coordIndex < mCoordXList.size(); ++coordIndex) {
					x = mCoordXList.get(coordIndex);
					y = mCoordYList.get(coordIndex);
					
					if(mHeatMapTileGrid[i][n].isCoordinateWithin(x, y) == true) {
						mHeatMapTileGrid[i][n].increaseWeight();
						
						mCoordXList.remove(coordIndex);
						mCoordYList.remove(coordIndex);
						coordIndex--;
					}
				}	
			}
		}
	}
	
	/*
	private int getMaxValue(HeatMapTile[][] mapTiles) {
        int maxValue = Integer.MIN_VALUE;
        
        for (int j = 0; j < mapTiles.length; j++) {
            for (int i = 0; i < mapTiles[j].length; i++) {
                if (mapTiles[j][i].getWeight() > maxValue) {
                    maxValue = mapTiles[j][i].getWeight();
                }
            }
        }
        
        return maxValue;
    }

	private int getMinValue(HeatMapTile[][] mapTiles) {
        int minValue = Integer.MAX_VALUE;
        
        for (int j = 0; j < mapTiles.length; j++) {
            for (int i = 0; i < mapTiles[j].length; i++) {
                if (mapTiles[j][i].getWeight() < minValue) {
                    minValue = mapTiles[j][i].getWeight();
                }
            }
        }
        
        return minValue;
    }
	
	private void normalizeWeightGrid(final double normalizationMinimum, final double normalizationMaximum) {
		final double maximumMinusMinimum = normalizationMaximum - normalizationMinimum;
		
		for(int i = 0; i < mHeatMapTileGrid.length; ++i) {
			for(int n = 0; n < mHeatMapTileGrid[i].length; ++n) {
				mHeatMapTileGrid[i][n].setWeight((int) Math.round(100.0 * normalizeWeight(mHeatMapTileGrid[i][n].getWeight(), maximumMinusMinimum, normalizationMinimum, normalizationMaximum)));
			}
		}
	}
	*/
	
	private double normalizeWeight(double value, double maximumMinusMinimum, double minimum, double maximum) {
	    return 1.0 / (maximumMinusMinimum) * (value - maximum) + 1.0; 
	}
	
	private void closeBlobTrackingScatterChart(boolean isReportPaginated) {
		if(isReportPaginated == true) {
			mChartBuilder.append("}]; Plotly.newPlot('myDiv', data, layout);");
		} else {
			mChartBuilder.append("}]; Plotly.newPlot('myDiv" + UNIQUE_DIV_NAME + "', data, layout);");
		}
	}
	
	private void buildHtmlFileContents(boolean isReportPaginated) {
		if(isReportPaginated == true) {
			final int indexOfHtmlHead = REPORT_PAGE_HEADER.indexOf("<head>") + 6;
			String startOfHeader = REPORT_PAGE_HEADER.substring(0, indexOfHtmlHead);
			String restOfHeader = REPORT_PAGE_HEADER.substring(indexOfHtmlHead, REPORT_PAGE_HEADER.length());
			// height 75% only for paginated
			mHtmlBuilder.append(startOfHeader);
			mHtmlBuilder.append("<script src=\"chart.js\"></script>");
			mHtmlBuilder.append(restOfHeader);
			mHtmlBuilder.append("<div id=\"myDiv\" style=\"height: 75%;\"></div><script src=\"");
			mHtmlBuilder.append(JAVASCRIPT_FILENAME_SHORT);
			mHtmlBuilder.append("\"></script>");//</body><html>");
			
			mHtmlBuilder.append("<br><div align=\"center\"><a href=\"");
			mHtmlBuilder.append(REPORT_ROOT);
			mHtmlBuilder.append("\">Home</a></div></body></html>");
		} else {
			mHtmlBuilder.append("<div id=\"myDiv" + UNIQUE_DIV_NAME + "\" style=\"height: 75%;\"></div><script src=\"");
			mHtmlBuilder.append(JAVASCRIPT_FILENAME_SHORT);
			mHtmlBuilder.append("\"></script>");//</body><html>");
			
			mHtmlBuilder.append("<br>");
		}
	}
	
	private void appendZEvents() {
		boolean isFirstRowEntry;
		boolean isFirstRow = true;
		
		for(int i = HEAT_MAP_TILE_GRID_HEIGHT - 1; i >= 0; --i) {//0; i < HEAT_MAP_TILE_GRID_HEIGHT; i++) {
			if(isFirstRow == true) {
				mChartBuilder.append("[");
				isFirstRow = false;
			} else {
				mChartBuilder.append(",[");
			}
			
			isFirstRowEntry = true;
			 
			for(int n = 0; n < HEAT_MAP_TILE_GRID_WIDTH; n++) {
				if(isFirstRowEntry == true) {
					mChartBuilder.append(mHeatMapTileGrid[i][n].getWeight());
					isFirstRowEntry = false;
				} else {
					mChartBuilder.append(",");
					mChartBuilder.append(mHeatMapTileGrid[i][n].getWeight());
				}
			}
			
			mChartBuilder.append("]");
		}
		
		mChartBuilder.append("]");
	}
	
	public void build(boolean isReportPaginated) {
		calculateTileWeights();
		
		//final double maximumWeightValue = getMaxValue(mHeatMapTileGrid);
		//final double minimumWeightValue = getMinValue(mHeatMapTileGrid);
	
		//normalizeWeightGrid(minimumWeightValue, maximumWeightValue);
		
		appendZEvents();
		
		closeBlobTrackingScatterChart(isReportPaginated);
		buildHtmlFileContents(isReportPaginated);
	}
	
	public String getChart() {
		return mChartBuilder.toString();
	}

	@Override
	public String getHtmlFile() {		
		return mHtmlBuilder.toString();
	}
}
