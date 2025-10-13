
def bezier_integrand(t, P0, P1, P2, P3):
    """
    Integral of a 2D Bézier curve.

    Parameters:
        t (float): The parameter for the Bézier curve.
        P0, P1, P2, P3 (tuple): Control points of the Bézier curve.

    Returns:
        tuple: The integral value at point `t`.
    """

    x = (1 - t)**3 * P0[0] + 3 * (1 - t)**2 * t * P1[0] + 3 * (1 - t) * t**2 * P2[0] + t**3 * P3[0]
    y = (1 - t)**3 * P0[1] + 3 * (1 - t)**2 * t * P1[1] + 3 * (1 - t) * t**2 * P2[1] + t**3 * P3[1]

    return x, y

# Example usage:
x = 0
P0 = (0, 0)
P1 = (0.2, 0)
P2 = (0.8, 0)  # Symmetric around (0.5, 0.5)
P3 = (1, 0)

t = x / 0.5

x_integrand, y_integrand = bezier_integrand(t, P0, P1, P2, P3)
print("Integral at x =", x, ":", y_integrand)
