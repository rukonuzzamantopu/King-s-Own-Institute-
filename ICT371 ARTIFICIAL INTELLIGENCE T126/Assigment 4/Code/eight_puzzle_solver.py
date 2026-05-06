import heapq
import re
import time

try:
    import matplotlib.pyplot as plt
    import matplotlib.patches as mpatches
    from matplotlib.colors import LinearSegmentedColormap
    HAS_MATPLOTLIB = True
except ImportError:
    plt = None
    mpatches = None
    LinearSegmentedColormap = None
    HAS_MATPLOTLIB = False


# GOAL STATE  (blank = 0, bottom-right corner)
GOAL_STATE = (1, 2, 3,
              4, 5, 6,
              7, 8, 0)

GOAL_POSITION = {v: (i // 3, i % 3) for i, v in enumerate(GOAL_STATE)}


#  HEURISTICS

def manhattan_distance(state):
    """
    Compute the Manhattan distance heuristic.
    Sum of horizontal + vertical distances each tile must travel to its goal.
    Admissible: never overestimates the true cost.
    """
    distance = 0
    for idx, tile in enumerate(state):
        if tile != 0:                            
            cur_row, cur_col = idx // 3, idx % 3
            goal_row, goal_col = GOAL_POSITION[tile]
            distance += abs(cur_row - goal_row) + abs(cur_col - goal_col)
    return distance


def misplaced_tiles(state):
    """
    Alternative heuristic: count tiles not in their goal position.
    Less informed than Manhattan distance but still admissible.
    """
    return sum(1 for i, tile in enumerate(state)
               if tile != 0 and tile != GOAL_STATE[i])


#  SOLVABILITY CHECK

def count_inversions(state):
    """
    Count the number of inversions in the flat state (ignoring blank).
    An inversion is a pair (i,j) where i < j but state[i] > state[j].
    """
    tiles = [t for t in state if t != 0]
    inversions = 0
    for i in range(len(tiles)):
        for j in range(i + 1, len(tiles)):
            if tiles[i] > tiles[j]:
                inversions += 1
    return inversions


def is_solvable(state):
    """
    For a 3×3 puzzle, a configuration is solvable if and only if
    the number of inversions is EVEN.
    """
    return count_inversions(state) % 2 == 0

#  SUCCESSOR GENERATION

# Direction: (row_delta, col_delta, label)
MOVES = [(-1, 0, "UP"), (1, 0, "DOWN"), (0, -1, "LEFT"), (0, 1, "RIGHT")]


def get_successors(state):
    """
    Generate all valid successor states from the current state.
    The blank tile (0) slides in each of the four cardinal directions.
    Returns list of (new_state, move_label).
    """
    blank_idx = state.index(0)
    row, col = blank_idx // 3, blank_idx % 3
    successors = []

    for dr, dc, label in MOVES:
        new_row, new_col = row + dr, col + dc
        if 0 <= new_row < 3 and 0 <= new_col < 3:
            new_idx = new_row * 3 + new_col
            new_state = list(state)
            # Swap blank with the target tile
            new_state[blank_idx], new_state[new_idx] = (
                new_state[new_idx], new_state[blank_idx]
            )
            successors.append((tuple(new_state), label))
    return successors


#  A* SEARCH

def astar_solve(initial_state, heuristic=manhattan_distance):
    if not is_solvable(initial_state):
        return None, 0, 0, 0.0

    start_time = time.time()

    # Priority queue entries: (f, g, state, path)
    # f = g + h  (total estimated cost)
    # g = cost so far (number of moves)
    h0 = heuristic(initial_state)
    frontier = [(h0, 0, initial_state, [(initial_state, "START")])]
    heapq.heapify(frontier)

    visited = {}            # state → best g seen
    nodes_expanded = 0
    max_frontier = 1

    while frontier:
        max_frontier = max(max_frontier, len(frontier))
        f, g, state, path = heapq.heappop(frontier)

        # Goal test
        if state == GOAL_STATE:
            elapsed = time.time() - start_time
            return path, nodes_expanded, max_frontier, elapsed

        # Skip if we have already expanded this state with a lower cost
        if state in visited and visited[state] <= g:
            continue
        visited[state] = g
        nodes_expanded += 1

        # Expand successors
        for next_state, move in get_successors(state):
            new_g = g + 1
            if next_state not in visited or visited[next_state] > new_g:
                new_h = heuristic(next_state)
                new_f = new_g + new_h
                new_path = path + [(next_state, move)]
                heapq.heappush(frontier, (new_f, new_g, next_state, new_path))

    elapsed = time.time() - start_time
    return None, nodes_expanded, max_frontier, elapsed      # unsolvable

#  VISUALISATION

def draw_board(ax, state, title="", step=None, highlight_idx=None):
    """Draw a single 3×3 puzzle board on the given Axes object."""
    ax.set_xlim(0, 3)
    ax.set_ylim(0, 3)
    ax.set_aspect('equal')
    ax.axis('off')

    if step is not None:
        ax.set_title(f"Step {step}\n{title}", fontsize=9, fontweight='bold',
                     pad=4)
    else:
        ax.set_title(title, fontsize=10, fontweight='bold', pad=4)

    for idx, tile in enumerate(state):
        row = 2 - (idx // 3)   # flip so row-0 is at top
        col = idx % 3

        if tile == 0:           # blank cell
            color = '#ECEFF1'
            text_color = '#ECEFF1'
        elif idx == highlight_idx:
            color = '#FF7043'
            text_color = 'white'
        else:
            color = '#1565C0'
            text_color = 'white'

        rect = mpatches.FancyBboxPatch(
            (col + 0.05, row + 0.05), 0.9, 0.9,
            boxstyle="round,pad=0.05",
            linewidth=1.5, edgecolor='white', facecolor=color
        )
        ax.add_patch(rect)

        if tile != 0:
            ax.text(col + 0.5, row + 0.5, str(tile),
                    ha='center', va='center',
                    fontsize=16, fontweight='bold', color=text_color)


def visualize_solution(path, filename="solution_path.png"):
    """
    Create a grid of board snapshots showing the solution path.
    Saves the figure to `filename`.
    """
    total_steps = len(path)
    # Show at most 12 snapshots evenly distributed
    max_shown = min(12, total_steps)
    step_indices = [int(i * (total_steps - 1) / (max_shown - 1))
                    for i in range(max_shown)]

    cols = 4
    rows = (max_shown + cols - 1) // cols
    fig, axes = plt.subplots(rows, cols,
                             figsize=(cols * 2.5, rows * 2.8),
                             facecolor='#263238')
    axes = axes.flatten()

    for i, ax_idx in enumerate(step_indices):
        state, move = path[ax_idx]
        draw_board(axes[i], state, title=move, step=ax_idx)

    # Hide unused axes
    for j in range(max_shown, len(axes)):
        axes[j].set_visible(False)

    fig.suptitle("8-Puzzle — A* Solution Path (selected steps)",
                 fontsize=13, color='white', fontweight='bold', y=1.01)
    if not HAS_MATPLOTLIB:
        print("  Skipping solution visualization because matplotlib is not installed.")
        return

    plt.tight_layout()
    plt.savefig(filename, dpi=150, bbox_inches='tight',
                facecolor='#263238')
    plt.close()
    print(f"  Visualization saved → {filename}")


def visualize_heuristic_comparison(results, filename="heuristic_comparison.png"):
    """Bar chart comparing Manhattan vs Misplaced-Tiles heuristics."""
    labels = ['Nodes Expanded', 'Max Frontier Size', 'Solution Length']
    manhattan_vals = [results['manhattan']['nodes'],
                      results['manhattan']['frontier'],
                      results['manhattan']['length']]
    misplaced_vals = [results['misplaced']['nodes'],
                      results['misplaced']['frontier'],
                      results['misplaced']['length']]

    x = range(len(labels))
    width = 0.35

    if not HAS_MATPLOTLIB:
        print("  Skipping heuristic comparison chart because matplotlib is not installed.")
        return

    fig, ax = plt.subplots(figsize=(8, 4.5), facecolor='#263238')
    ax.set_facecolor('#37474F')

    bars1 = ax.bar([i - width/2 for i in x], manhattan_vals, width,
                   label='Manhattan Distance', color='#1565C0', alpha=0.9)
    bars2 = ax.bar([i + width/2 for i in x], misplaced_vals, width,
                   label='Misplaced Tiles', color='#FF7043', alpha=0.9)

    ax.set_xticks(list(x))
    ax.set_xticklabels(labels, color='white', fontsize=11)
    ax.set_ylabel('Count', color='white', fontsize=11)
    ax.set_title('Heuristic Comparison: Manhattan vs Misplaced Tiles',
                 color='white', fontsize=12, fontweight='bold')
    ax.tick_params(colors='white')
    ax.spines[:].set_color('#546E7A')
    ax.legend(facecolor='#37474F', labelcolor='white')

    for bar in bars1:
        ax.text(bar.get_x() + bar.get_width()/2, bar.get_height() + 0.5,
                str(int(bar.get_height())),
                ha='center', va='bottom', color='white', fontsize=9)
    for bar in bars2:
        ax.text(bar.get_x() + bar.get_width()/2, bar.get_height() + 0.5,
                str(int(bar.get_height())),
                ha='center', va='bottom', color='white', fontsize=9)

    plt.tight_layout()
    plt.savefig(filename, dpi=150, bbox_inches='tight', facecolor='#263238')
    plt.close()
    print(f"  Comparison chart saved → {filename}")


#  PRINT UTILITIES

def print_board(state):
    """Pretty-print a 3×3 board to the terminal."""
    for i in range(3):
        row = state[i*3:(i+1)*3]
        print(" ".join(str(t) if t != 0 else '_' for t in row))
    print()


def print_solution(path):
    """Print every step of the solution to stdout."""
    print("=" * 40)
    print(f"  Solution found in {len(path) - 1} moves")
    print("=" * 40)
    for step, (state, move) in enumerate(path):
        print(f"Step {step:>3} | Move: {move}")
        print_board(state)


#  MAIN

def solve_and_report(initial_state):
    """
    Full pipeline: check solvability, run A* with both heuristics,
    print results, and produce visualizations.
    """
    print("\n" + "=" * 50)
    print("  8-PUZZLE SOLVER  (A* Search)")
    print("=" * 50)
    print("\nInitial state:")
    print_board(initial_state)
    print(f"Goal state:   1 2 3 / 4 5 6 / 7 8 _")
    print(f"Inversions:   {count_inversions(initial_state)}")
    print(f"Solvable:     {is_solvable(initial_state)}\n")

    if not is_solvable(initial_state):
        print("This configuration has no solution.")
        return

    results = {}

    # Run A* with Manhattan distance 
    print("Running A* with Manhattan distance heuristic …")
    path_m, nodes_m, frontier_m, time_m = astar_solve(
        initial_state, heuristic=manhattan_distance)
    results['manhattan'] = {
        'nodes': nodes_m, 'frontier': frontier_m,
        'length': len(path_m) - 1, 'time': time_m
    }
    print(f"  Moves:          {len(path_m) - 1}")
    print(f"  Nodes expanded: {nodes_m}")
    print(f"  Max frontier:   {frontier_m}")
    print(f"  Time:           {time_m:.4f}s\n")

    # Run A* with Misplaced Tiles
    print("Running A* with Misplaced Tiles heuristic …")
    path_t, nodes_t, frontier_t, time_t = astar_solve(
        initial_state, heuristic=misplaced_tiles)
    results['misplaced'] = {
        'nodes': nodes_t, 'frontier': frontier_t,
        'length': len(path_t) - 1, 'time': time_t
    }
    print(f"  Moves:          {len(path_t) - 1}")
    print(f"  Nodes expanded: {nodes_t}")
    print(f"  Max frontier:   {frontier_t}")
    print(f"  Time:           {time_t:.4f}s\n")

    #Print step-by-step for Manhattan solution 
    print_solution(path_m)

    # Save visualizations 
    visualize_solution(path_m, "solution_path.png")
    visualize_heuristic_comparison(results, "heuristic_comparison.png")

    return path_m, results


def get_initial_state_from_user():
    """Prompt the user for an initial 8-puzzle state and validate it."""
    print("\nEnter the initial puzzle state as 9 numbers from 0 to 8.")
    print("Use 0 to represent the blank tile, separated by spaces or commas.")
    print("Example: 1 2 3 4 0 6 7 5 8")

    while True:
        user_input = input("Initial state: ").strip()
        tokens = re.split(r"[\s,]+", user_input)

        if len(tokens) != 9:
            print("Please enter exactly 9 values.")
            continue

        try:
            values = [int(token) for token in tokens]
        except ValueError:
            print("All values must be integers from 0 to 8.")
            continue

        if sorted(values) != list(range(9)):
            print("Values must be a permutation of 0 through 8 with no duplicates.")
            continue

        return tuple(values)


#  Example initial configurations
if __name__ == "__main__":
    INITIAL = get_initial_state_from_user()
    solve_and_report(INITIAL)
