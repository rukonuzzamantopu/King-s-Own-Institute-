# 8-Puzzle Solver — ICT371 Assessment 4

## Requirements
- Python 3.8+
- matplotlib  (pip install matplotlib)

## How to Run

```bash
pip install matplotlib
python eight_puzzle_solver.py
```

## Configuration
Open `eight_puzzle_solver.py` and edit the `INITIAL` variable near the bottom
of the file to test different starting boards. Three presets are available:

```python
easy   = (1,2,3, 4,0,6, 7,5,8)
medium = (1,2,3, 5,0,6, 4,7,8)
hard   = (8,1,3, 4,0,2, 7,6,5)

INITIAL = hard   # ← change here
```

Blank tile is represented as 0.

## Output
- Step-by-step solution printed to console
- `solution_path.png` — visual board snapshots
- `heuristic_comparison.png` — bar chart comparing heuristics
